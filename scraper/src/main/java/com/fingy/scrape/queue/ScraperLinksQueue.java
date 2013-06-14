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

	public ScraperLinksQueue() {
		queuedLinks = new LinkedList<String>();
		visitedLinks = new LinkedHashSet<String>();
	}

	public Collection<String> getVisitedLinks() {
		return Collections.unmodifiableCollection(visitedLinks);
	}

	public synchronized int getSize() {
		return queuedLinks.size();
	}

	public synchronized boolean isEmpty() {
		return queuedLinks.isEmpty();
	}

	public synchronized int getVisitedSize() {
		return visitedLinks.size();
	}

	public synchronized void add(String linkToEnqueue) {
		queuedLinks.add(linkToEnqueue);
		notifyAll();
	}

	public synchronized void markVisited(String visitedLink) {
		visitedLinks.add(visitedLink);
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

	public synchronized void addAndVisitIfNotVisited(final String linkToEnqueue) {
		if (!isAlreadyVisited(linkToEnqueue))
			enqueueAndVisit(linkToEnqueue);
	}

	private boolean isAlreadyVisited(String linkToEnqueue) {
		return visitedLinks.contains(linkToEnqueue);
	}

	private void enqueueAndVisit(String linkToEnqueue) {
		markVisited(linkToEnqueue);
		add(linkToEnqueue);
	}

	public boolean delayedIsEmpty() throws InterruptedException {
		if (queuedLinks.isEmpty())
			Thread.sleep(3000);
		return queuedLinks.isEmpty();
	}

}
