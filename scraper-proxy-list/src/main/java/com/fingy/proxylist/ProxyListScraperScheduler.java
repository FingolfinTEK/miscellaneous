package com.fingy.proxylist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.fingy.proxylist.scrape.ProxyScraper;

public class ProxyListScraperScheduler {

    private static final String URL = "http://hidemyass.com/proxy-list/";


    public List<ProxyInfo> getProxies() throws InterruptedException, ExecutionException {
        List<ProxyInfo> proxies = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletionService<Collection<ProxyInfo>> completion = new ExecutorCompletionService<>(executor);

        completion.submit(new ProxyScraper(URL));
        for(int i = 2; i <= 15; i++) {
            completion.submit(new ProxyScraper(URL + i));
        }

        executor.shutdown();
        for (int i = 0; i < 15; i++) {
            Future<Collection<ProxyInfo>> proxiesFromPage = completion.take();
            proxies.addAll(proxiesFromPage.get());
        }

        return proxies;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println(new ProxyListScraperScheduler().getProxies().toString());
    }
}
