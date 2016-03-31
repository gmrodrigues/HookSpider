package com.gmrodrigues.hookspider.app;

import com.gmrodrigues.hookspider.downloader.Downloader;
import com.gmrodrigues.hookspider.downloader.DownloaderFactory;
import com.gmrodrigues.hookspider.downloader.registry.DownloaderRegistryTool;
import com.gmrodrigues.hookspider.model.DownloaderStateModel;
import com.gmrodrigues.hookspider.scrapper.ScrapperFactory;
import com.gmrodrigues.hookspider.scrapper.ScrapperXquery;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;


public class App
{
    public static void main(String[] args) throws ParseException, IOException, ParserConfigurationException, SAXException
    {

        CommandLineParser parser = new GnuParser();
        Options options = getOptions();
        CommandLine line = parser.parse(options, args);
        String confFilename = line.getOptionValue("f");
        String baseDirname = line.getOptionValue("d");

        File confFile = null;
        File baseDir = null;
        DownloaderFactory factory = null;

        try {
            confFile = new File(confFilename).getCanonicalFile();

            if (baseDirname == null) {
                baseDirname = confFile.getParent();
            }

            baseDir = new File(baseDirname).getCanonicalFile();

            factory = new DownloaderFactory();
            factory.setBaseDir(baseDir);
            factory.setConfFile(confFile);
            factory.load();
        }
        catch (ConfigurationException e) {
            System.err.println("Problem loading configuration: " + e.getMessage());
            System.exit(0);
        }
        catch (Exception e) {
            e.printStackTrace();
            printHelpAndQuit(options);
        }

        Downloader dw = factory.newInstance();
        dw.downloadAll();

        File reportFile = new File(baseDir, "wrd_registry." + confFile.getName());

        DownloaderRegistryTool rc = new DownloaderRegistryTool(dw.getState());

        PrintStream out = new PrintStream(reportFile);
        rc.writeModelToXml(out);
        IOUtils.closeQuietly(out);
        System.out.println("Report writen to file " + reportFile.toString());

        DownloaderStateModel sm = new DownloaderStateModel();
        sm.setBaseDir(baseDir);
        rc.setDownloaderState(sm);
        rc.loadModelFromXml(new FileInputStream(reportFile));
        rc.writeModelToXml(new PrintStream(new File("state.xml")));


        ScrapperFactory sf = new ScrapperFactory();
        sf.loadFromConfigFileAndDownloadedUrisMap(confFile, dw.getState().getDownloadedURIs());

        Map<String, ScrapperXquery> scrappersMap = sf.getInstancesMap();
        for (String scrapperName : scrappersMap.keySet()) {
            ScrapperXquery sx = scrappersMap.get(scrapperName);
            try {
                sx.scrap();
            }
            catch (SAXException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        System.exit(0);
    }

    @SuppressWarnings("static-access")
    private static Options getOptions()
    {
        Options options = new Options();
        Option confFilename = OptionBuilder.withArgName("CONF_FILE")
                .hasArg()
                .withDescription("Use CONF_FILE as config file (usualy a *.rtv.xml file)")
                .create("f");

        Option baseDirname = OptionBuilder.withArgName("TARGET_BASE_DIR")
                .hasArg()
                .withDescription("Save downloaded content inside TARGET_BASE_DIR. Defaults to CONF_FILE's parent dir.")
                .create("d");

        options.addOption(confFilename);
        options.addOption(baseDirname);
        return options;

    }

    private static void printHelpAndQuit(Options options)
    {
        String execName = new File(App.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("retriever [-d TARGET_BASE_DIR] -f CONF_FILE ", options);
        System.exit(0);
    }
}
