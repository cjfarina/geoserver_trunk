package org.geoserver.gss.internal.atom.builders;

import java.util.Date;
import java.util.List;

import org.geoserver.gss.internal.atom.CategoryImpl;
import org.geoserver.gss.internal.atom.ContentImpl;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.LinkImpl;
import org.geoserver.gss.internal.atom.PersonImpl;
import org.springframework.util.Assert;

public class EntryBuilder {

    private List<PersonImpl> author;

    private List<CategoryImpl> category;

    private ContentImpl content;

    private List<PersonImpl> contributor;

    private String id;

    private List<LinkImpl> link;

    private Date published;

    private String rights;

    private String source;

    private String summary;

    private String title;

    private Date updated;

    public EntryImpl build() {
        Assert.notNull(id, "Id can't be null");
        
        final EntryImpl entry = new EntryImpl();
        entry.setAuthor(author);
        entry.setCategory(category);
        entry.setContent(content);
        entry.setContributor(contributor);
        entry.setId(id);
        entry.setLink(link);
        entry.setPublished(published);
        entry.setRights(rights);
        entry.setSource(source);
        entry.setSummary(summary);
        entry.setTitle(title);
        entry.setUpdated(updated);
        
        return entry;
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

    public ContentImpl getContent() {
        return content;
    }

    public void setContent(ContentImpl content) {
        this.content = content;
    }

    public List<PersonImpl> getContributor() {
        return contributor;
    }

    public void setContributor(List<PersonImpl> contributor) {
        this.contributor = contributor;
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

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

}
