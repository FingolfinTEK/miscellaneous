package com.fingy.scrape.queue;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ScraperLinksQueue {

    private final Set<String> visitedLinks;
    private final Set<String> queuedLinksSet;

    public ScraperLinksQueue() {
        visitedLinks = new LinkedHashSet<String>();
        queuedLinksSet = new LinkedHashSet<String>();
    }

    public Collection<String> getVisitedLinks() {
        return Collections.unmodifiableCollection(visitedLinks);
    }

    public Collection<String> getQueuedLinks() {
        return Collections.unmodifiableCollection(queuedLinksSet);
    }

    public synchronized int getSize() {
        return queuedLinksSet.size();
    }

    public synchronized boolean isEmpty() {
        return queuedLinksSet.isEmpty();
    }

    public synchronized boolean delayedIsEmpty(final long timeoutMillis) {
        if (queuedLinksSet.isEmpty()) {
            waitWithTimeout(timeoutMillis);
        }
        return queuedLinksSet.isEmpty();
    }

    private void waitWithTimeout(final long timeoutMillis) {
        try {
            wait(timeoutMillis);
        } catch (InterruptedException e) {
        }
    }

    public synchronized int getVisitedSize() {
        return visitedLinks.size();
    }

    public synchronized void add(final String linkToEnqueue) {
        if (!queuedLinksSet.contains(linkToEnqueue)) {
            queuedLinksSet.add(linkToEnqueue);
            notifyAll();
        }
    }

    public synchronized int addAllIfNotVisited(final Collection<String> linksToAdd) {
        int numberAdded = 0;

        for (String linkToEnqueue : linksToAdd) {
            if (isNotAlreadyQueuedAndNotVisited(linkToEnqueue)) {
                numberAdded++;
                queuedLinksSet.add(linkToEnqueue);
            }
        }

        notifyAll();
        return numberAdded;
    }

    private boolean isNotAlreadyQueuedAndNotVisited(final String linkToEnqueue) {
        return !queuedLinksSet.contains(linkToEnqueue) && !visitedLinks.contains(linkToEnqueue);
    }

    public synchronized void markVisited(final String linkToMarkVisited) {
        visitedLinks.add(linkToMarkVisited);
    }

    public synchronized void markAllVisited(final Collection<String> linksToMarkVisited) {
        visitedLinks.addAll(linksToMarkVisited);
    }

    public synchronized String take() throws InterruptedException {
        while (queuedLinksSet.isEmpty()) {
            wait();
        }

        return takeFromQueuedLinksSet();
    }

    private String takeFromQueuedLinksSet() {
        Iterator<String> iterator = queuedLinksSet.iterator();
        String taken = iterator.next();
        iterator.remove();

        return taken;
    }

    public synchronized String take(final long timeout) throws InterruptedException {
        wait(timeout);
        return takeFromQueuedLinksSet();
    }

    public String peek() {
        return queuedLinksSet.iterator().next();
    }

    public synchronized void addIfNotVisited(final String linkToEnqueue) {
        if (!isAlreadyVisited(linkToEnqueue)) {
            add(linkToEnqueue);
        }
    }

    private boolean isAlreadyVisited(final String linkToEnqueue) {
        return visitedLinks.contains(linkToEnqueue);
    }

}
