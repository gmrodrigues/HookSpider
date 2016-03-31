package com.gmrodrigues.hookspider.downloader;

import com.gmrodrigues.hookspider.downloadclient.UriFetcher;
import com.gmrodrigues.hookspider.linkextractor.LinkExtrator;
import com.gmrodrigues.hookspider.model.DownloadedUriModel;
import com.gmrodrigues.hookspider.model.DownloaderConfigModel;
import com.gmrodrigues.hookspider.model.DownloaderStateModel;
import com.gmrodrigues.hookspider.model.UriScannedForLinksModel;
import com.gmrodrigues.taskpooler.TaskPool;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Downloader
{
    private TaskPool downPool = new TaskPool();

    private DownloaderStateModel state;
    private DownloaderConfigModel config;

    public Downloader(File baseDir)
    {
        config = new DownloaderConfigModel();
        state = new DownloaderStateModel();
        state.setBaseDir(baseDir);
    }

    public boolean tasksFinished()
    {
        return downPool.getActiveTasksQuant() < 1;
    }

    public boolean finished()
    {
        return state.downloadedAll() && tasksFinished();
    }

    public void downloadAll()
    {
        long start = System.currentTimeMillis();
        try {
            while (true) {
                if (!state.downloadedAll()) {
                    downloadNext();
                    continue;
                }
                if (!tasksFinished()) {
                    Thread.sleep(500);
                    continue;
                }
                break;
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        long done = System.currentTimeMillis();
        System.err.println(" -  Total download time "
                + ((done - start) / 1000.0) + "\n");
    }

    private void downloadNext() throws InterruptedException
    {
        downPool.runTask(new Thread(new DownloadRunnable()));
    }

    private class DownloadRunnable implements Runnable
    {
        @Override
        public void run()
        {
            try {
                URI uri = state.pollUriFromCrawlQueue();
                boolean isCrawlUri = false;
                boolean isDownloadUri = false;
                if (uri != null) {
                    isCrawlUri = true;
                }
                else {
                    uri = state.pollUriFromDownloadQueue();
                    if (uri != null) {
                        isDownloadUri = true;
                    }
                }
                if (!(isCrawlUri || isDownloadUri)) {
                    return;
                }

                System.err.println("[ ]           " + uri.toString());
                File toFile = state.getFileMappedFromUri(uri);
                toFile.getParentFile().mkdirs();

                UriFetcher fetcher = new UriFetcher();
                fetcher.setUsername(config.getAuth().getUsername());
                fetcher.setPassword(config.getAuth().getPassword());

                Calendar started = Calendar.getInstance();
                fetcher.copy(uri, toFile);
                Calendar finished = Calendar.getInstance();

                DownloadedUriModel du = new DownloadedUriModel(uri, toFile);
                du.setBytesCopied(toFile.length());
                du.setStartedTime(started);
                du.setFinishedTime(finished);
                du.setRefererUri(state.getRefererUriFromUri(uri));
                du.setFoundWithExtractor(state.getExtratorUsedToFindUri(uri));

                du.getPassedUriTestNames().addAll(
                        config.getCrawlTester().getPassedTestsNames(uri));
                du.getPassedUriTestNames().addAll(
                        config.getDownloadTester().getPassedTestsNames(uri));

                System.err.println("[x] "
                        + String.format("% 4.0f KB/s ",
                        (du.getCopiedBytesPerSecond() / 1024))
                        + uri.toString());

                state.markAsDownloaded(du);
                if (isCrawlUri) {
                    crawlInto(du);
                }


            }
            catch (ClientProtocolException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void crawlInto(DownloadedUriModel du) throws IOException,
                MalformedURLException
        {
            URI uri = du.getUri();
            File file = du.getToFile();


            List<URI> urisFound;
            LinkExtrator extractor = config.findLinkExtractorForUri(uri, config);

            UriScannedForLinksModel scanned = new UriScannedForLinksModel();
            scanned.setUri(uri);
            scanned.setStartedTime(Calendar.getInstance());

            if (extractor != null) {
                urisFound = extractor.getUris(file, null, uri);
                scanned.setNameOfLinkExtractorUsed(extractor.getName());
            }
            else {
                urisFound = new ArrayList<URI>();
                Document doc = Jsoup.parse(file, null, uri.toString());
                Elements links = doc.select("a");

                for (Element link : links) {
                    String href = link.attr("href");
                    URL baseUrl = uri.toURL();
                    try {
                        URL linkURL = new URL(baseUrl, href);
                        URI foundUri = linkURL.toURI();
                        urisFound.add(foundUri);
                    }
                    catch (Exception e) {
                        // TODO Tratar melhor url doidona
                        System.err.println("Url doidona: " + baseUrl.toString()
                                + " => " + href);
                        e.printStackTrace();
                    }
                }
            }
            scanned.setFinishedTime(Calendar.getInstance());
            for (URI foundUri : urisFound) {
                state.addUri(foundUri, config);
                scanned.getUrisExtracted().add(foundUri);
            }
            state.markAsScannedForLinks(scanned);
            return;
        }

    }

    // Getters & Setters

    public DownloaderStateModel getState()
    {
        return state;
    }

    public void setState(DownloaderStateModel state)
    {
        this.state = state;
    }

    public DownloaderConfigModel getConfig()
    {
        return config;
    }

    public void setConfig(DownloaderConfigModel config)
    {
        this.config = config;
    }

    // Overrides

    @Override
    public String toString()
    {
        return "Downloader [tasksFinished()=" + tasksFinished()
                + ", finished()=" + finished() + ", getState()=" + getState()
                + ", getConfig()=" + getConfig() + "]";
    }

}
