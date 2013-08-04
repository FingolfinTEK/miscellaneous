package com.fingy.scrape.queue;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class ScraperLinksQueue {

    private final Set<String> visitedLinks;
    private final Deque<String> queuedLinks;
    private final Set<String> queuedLinksSet;

    public ScraperLinksQueue() {
        visitedLinks = new LinkedHashSet<String>();
        queuedLinks = new LinkedList<String>();
        queuedLinksSet = new LinkedHashSet<String>();
    }

    public Collection<String> getVisitedLinks() {
        return Collections.unmodifiableCollection(visitedLinks);
    }

    public Collection<String> getQueuedLinks() {
        return Collections.unmodifiableCollection(queuedLinksSet);
    }

    public synchronized int getSize() {
        return queuedLinks.size();
    }

    public synchronized boolean isEmpty() {
        return queuedLinks.isEmpty();
    }

    public synchronized boolean delayedIsEmpty(final long timeoutMillis) {
        if (queuedLinks.isEmpty()) {
            waitWithTimeout(timeoutMillis);
        }
        return queuedLinks.isEmpty();
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
            queuedLinks.add(linkToEnqueue);
            queuedLinksSet.add(linkToEnqueue);
            notifyAll();
        }
    }

    public synchronized int addAllIfNotVisited(final Collection<String> linksToAdd) {
        int numberAdded = 0;

        for (String linkToEnqueue : linksToAdd) {
            if (isNotAlreadyQueuedAndNotVisited(linkToEnqueue)) {
                numberAdded++;
                queuedLinks.add(linkToEnqueue);
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
        while (queuedLinks.isEmpty()) {
            wait();
        }

        String taken = queuedLinks.poll();
        if (taken != null) {
            queuedLinksSet.remove(taken);
        }

        return taken;
    }

    public synchronized String take(final long timeout) throws InterruptedException {
        wait(timeout);

        String taken = queuedLinks.poll();
        queuedLinksSet.remove(taken);
        return taken;
    }

    public String peek() {
        return queuedLinks.peek();
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
