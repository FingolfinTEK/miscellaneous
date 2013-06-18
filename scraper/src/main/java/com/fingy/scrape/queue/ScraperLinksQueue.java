package com.fingy.scrape.queue;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class ScraperLinksQueue {

	private final Set<String> visitedLinks;
	private final Deque<String> queuedLinks;

	public ScraperLinksQueue() {
		queuedLinks = new LinkedList<>();
		visitedLinks = new LinkedHashSet<>();
	}

	public Collection<String> getVisitedLinks() {
		return Collections.unmodifiableCollection(visitedLinks);
	}

	public Collection<String> getQueuedLinks() {
		return Collections.unmodifiableCollection(new HashSet<>(queuedLinks));
	}

	public synchronized int getSize() {
		return queuedLinks.size();
	}

	public synchronized boolean isEmpty() {
		return queuedLinks.isEmpty();
	}

	public synchronized boolean delayedIsEmpty(long timeoutMillis) {
		if (queuedLinks.isEmpty())
			waitWithTimeout(timeoutMillis);
		return queuedLinks.isEmpty();
	}

	private void waitWithTimeout(long timeoutMillis) {
		try {
			wait(timeoutMillis);
		} catch (InterruptedException e) {
		}
	}

	public synchronized int getVisitedSize() {
		return visitedLinks.size();
	}

	public synchronized void add(String linkToEnqueue) {
		queuedLinks.add(linkToEnqueue);
		notifyAll();
	}

	public synchronized void addAllIfNotVisited(Collection<String> linksToAdd) {
		for (String linkToEnqueue : linksToAdd)
			queuedLinks.add(linkToEnqueue);

		notifyAll();
	}

	public synchronized void markVisited(String linkToMarkVisited) {
		visitedLinks.add(linkToMarkVisited);
	}

	public synchronized void markAllVisited(Collection<String> linksToMarkVisited) {
		visitedLinks.addAll(linksToMarkVisited);
	}

	public synchronized String take() throws InterruptedException {
		while (queuedLinks.isEmpty())
			wait();

		return queuedLinks.poll();
	}

	public synchronized String take(long timeout) throws InterruptedException {
		wait(timeout);
		return queuedLinks.poll();
	}

	public String peek() {
		return queuedLinks.peek();
	}

	public synchronized void addIfNotVisited(final String linkToEnqueue) {
		if (!isAlreadyVisited(linkToEnqueue))
			add(linkToEnqueue);
	}

	private boolean isAlreadyVisited(String linkToEnqueue) {
		return visitedLinks.contains(linkToEnqueue);
	}

}
