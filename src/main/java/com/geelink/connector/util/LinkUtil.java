package com.geelink.connector.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class LinkUtil {
    private Set<String> links = new HashSet<>();
    private int depth;

    public LinkUtil(int depth) {
        this.depth = depth;
    }

    public Set<String> getLinks() {
        return links;
    }

    public void getPageLinks(String URL) {
        //4. Check if you have already crawled the URLs
        //(we are intentionally not checking for duplicate content in this example)
        if (!links.contains(URL)) {
            try {
                //4. (i) If not add it to the index
                if ((URL.endsWith(".html") || URL.endsWith(".htm")) && !URL.contains("index.htm")) {
                    links.add(URL);
                    log.info("adding url:{}", URL);
                }
                //2. Fetch the HTML code
                Document document = Jsoup.connect(URL).get();
                //3. Parse the HTML to extract links to other URLs
                Elements linksOnPage = document.select("a[href]");

                //5. For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage) {
                    String url = page.attr("abs:href");
                    log.info("url: {}", url);
                    getSubPageLinks(page.attr("abs:href"), 0);
                }

            } catch (IOException e) {
                log.error("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    public boolean getSubPageLinks(String URL, int curDepth) {
        if (curDepth >= depth) return false;
        log.info("processing url:{}", URL);
        curDepth++;
        //4. Check if you have already crawled the URLs
        //(we are intentionally not checking for duplicate content in this example)
        if (!links.contains(URL)) {
            try {
                //4. (i) If not add it to the index
                if ((URL.endsWith(".html") || URL.endsWith(".htm")) && !URL.contains("index.htm")) {
                    links.add(URL);
                    log.info("adding url:{}", URL);
                }
                if (curDepth >= depth) return false;
                //2. Fetch the HTML code
                Document document = Jsoup.connect(URL).get();
                //3. Parse the HTML to extract links to other URLs
                Elements linksOnPage = document.select("a[href]");

                //5. For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage) {
                    if(!getSubPageLinks(page.attr("abs:href"), curDepth)) return false;
                }

            } catch (IOException e) {
                log.error("For '" + URL + "': " + e.getMessage());
            }
        }

        return true;
    }
}
