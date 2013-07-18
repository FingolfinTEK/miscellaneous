package com.fingy.ehentai;

public class MangaInfo {

    private final String title;
    private final String url;
    private final String images;
    private final String tags;
    private final String coverImageUrl;

    public MangaInfo(String title, String url, String images, String tags, String coverImageUrl) {
        this.title = title;
        this.url = url;
        this.images = images;
        this.tags = tags;
        this.coverImageUrl = coverImageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getImages() {
        return images;
    }

    public String getTags() {
        return tags;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((images == null) ? 0 : images.hashCode());
        result = prime * result + ((tags == null) ? 0 : tags.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MangaInfo other = (MangaInfo) obj;
        if (images == null) {
            if (other.images != null)
                return false;
        } else if (!images.equals(other.images))
            return false;
        if (tags == null) {
            if (other.tags != null)
                return false;
        } else if (!tags.equals(other.tags))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(title);
        builder.append("#");
        builder.append(url);
        builder.append("#");
        builder.append(images);
        builder.append("#");
        builder.append(tags);
        builder.append("#");
        builder.append(coverImageUrl);
        return builder.toString();
    }

    public static MangaInfo fromString(String line) {
        String[] data = line.split("#");
        return new MangaInfo(data[0], data[1], data[2], data[3], data[4]);
    }

}
