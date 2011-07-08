package org.geoserver.bxml.atom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.geoserver.gss.internal.atom.CategoryImpl;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geoserver.gss.internal.atom.GeneratorImpl;
import org.geoserver.gss.internal.atom.LinkImpl;
import org.geoserver.gss.internal.atom.PersonImpl;
import org.springframework.util.Assert;

public final class FeedBuilder {

    private List<PersonImpl> author = new ArrayList<PersonImpl>();

    private List<CategoryImpl> category = new ArrayList<CategoryImpl>();

    private List<PersonImpl> contributor = new ArrayList<PersonImpl>();

    private GeneratorImpl generator;

    private String icon;

    private String id;

    private List<LinkImpl> link = new ArrayList<LinkImpl>();

    // private XXX logo

    private String rights;

    private String subtitle;

    private String title;

    private Date updated;

    @SuppressWarnings("unchecked")
    private Iterator<EntryImpl> entry = Collections.EMPTY_LIST.iterator();

    public Iterator<EntryImpl> getEntry() {
        return entry;
    }

    public void setEntry(Iterator<EntryImpl> entry) {
        this.entry = entry;
    }

    private Long startPosition;

    private Long maxEntries;

    public FeedImpl build() {
        Assert.notNull(id, "Id can't be null");

        final FeedImpl feed = new FeedImpl();
        feed.setAuthor(author);
        feed.setCategory(category);
        feed.setContributor(contributor);
        feed.setGenerator(generator);
        feed.setIcon(icon);
        feed.setId(id);
        feed.setLink(link);
        feed.setRights(rights);
        feed.setSubtitle(subtitle);
        feed.setTitle(title);
        feed.setUpdated(updated);
        feed.setEntry(entry);
        feed.setStartPosition(startPosition);
        feed.setMaxEntries(maxEntries);

        return feed;
    }

    public List<PersonImpl> getAuthor() {
        return author;
    }

    public void setAuthor(List<PersonImpl> author) {
        this.author = author;
    }

    public void addAuthor(PersonImpl author) {
        this.author.add(author);
    }

    public List<CategoryImpl> getCategory() {
        return category;
    }

    public void setCategory(List<CategoryImpl> category) {
        this.category = category;
    }

    public void addCategory(CategoryImpl category) {
        this.category.add(category);
    }

    public List<PersonImpl> getContributor() {
        return contributor;
    }

    public void setContributor(List<PersonImpl> contributor) {
        this.contributor = contributor;
    }

    public void addContributor(PersonImpl contributor) {
        this.contributor.add(contributor);
    }

    public GeneratorImpl getGenerator() {
        return generator;
    }

    public void setGenerator(GeneratorImpl generator) {
        this.generator = generator;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<LinkImpl> getLink() {
        return link;
    }

    public void setLink(List<LinkImpl> link) {
        this.link = link;
    }

    public void addLink(LinkImpl link) {
        this.link.add(link);
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Long startPosition) {
        this.startPosition = startPosition;
    }

    public Long getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(Long maxEntries) {
        this.maxEntries = maxEntries;
    }
}
