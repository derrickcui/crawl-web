package com.geelink.connector.crawl.processor;

import com.geelink.connector.model.CrawlerRequest;
import com.geelink.connector.util.PageUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.geelink.connector.crawl.LevelLimitScheduler.LEVEL_KEY;
import static com.geelink.connector.util.PageUtil.*;

@Getter
@Setter
@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WenshuCourtProcessor implements PageProcessor {
    private CrawlerRequest request;
    private Pattern urlPattern;
    private static final String DEFAULT_PATTERN = "html$";
    private List<String> domainList;
    private String chromeDriverFolder;

    public WenshuCourtProcessor(CrawlerRequest request, String chromeDriverFolder) {
        this.request = request;
        this.chromeDriverFolder = chromeDriverFolder;
        String pattern = StringUtils.isBlank(request.getPattern())? DEFAULT_PATTERN: request.getPattern();
        this.urlPattern = Pattern.compile(pattern);
        this.domainList = request.getUrls().stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setSleepTime(1000)
            .setTimeOut(10000)
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
            .addHeader("Connection", "keep-alive")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Content-Type",
                    "application/x-www-form-urlencoded;charset=utf-8")
            .addHeader(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
    // 用来存储cookie信息
    private Set<Cookie> cookies;


    @Override
    public void process(Page page) {
        int currentLevel = PageUtil.getLevel(page);

        List<String> subLinks = page.getHtml().links().all();

        if (CollectionUtils.isNotEmpty(subLinks) ) {
            List<Request> targets = subLinks.stream()
                    .filter(t -> domainList.stream().anyMatch(t::contains))
                    .map(Request::new)
                    .map(r -> r.putExtra(LEVEL_KEY, currentLevel + 1))
                    .toList();
            targets.forEach(page::addTargetRequest);
        }

        if (PageUtil.isMatch(request, page.getUrl().get(), urlPattern) ) {
            page.putField(FIELD_GL_SOURCE, page.getUrl().toString());
            page.putField(FIELD_TITLE, page.getHtml().xpath("//title")
                    .get().replace("<title>", "")
                    .replace("</title>", ""));
            page.putField(FIELD_CONTENT, page.getHtml().smartContent().get());
        } else {
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        // 将获取到的cookie信息添加到webmagic中
        for (Cookie cookie : cookies) {
            site.addCookie(cookie.getName(), cookie.getValue());
        }

        return site;
    }

    public void login() {
        // set crawler driver
        if ( StringUtils.isBlank(request.getChromeVersion()) || StringUtils.isBlank(request.getPlatform()) ) {
            log.error("No Chrome version or platform is provided, no need to login");
            return;
        }

        String deriveFile = null;
        if ( request.getPlatform().toLowerCase().startsWith("win") ) {
            deriveFile = this.chromeDriverFolder + "/chromedriver.win." + request.getChromeVersion() + ".exe";
        } else if ( request.getPlatform().toLowerCase().startsWith("mac") ) {
            deriveFile = this.chromeDriverFolder + "/chromedriver.mac." + request.getChromeVersion();
        } else if ( request.getPlatform().toLowerCase().startsWith("lin") ) {
            deriveFile = this.chromeDriverFolder + "/chromedriver.linux." + request.getChromeVersion();
        }

        log.info("Chrome driver file is {}", deriveFile);
        System.setProperty("webdriver.chrome.driver", Objects.requireNonNull(deriveFile));

        WebDriver driver = new ChromeDriver();
        driver.get(request.getUrls().get(0));// 打开网址

        // 防止页面未能及时加载出来而设置一段时间延迟
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Failed to pause thread", e);
        }

        // 设置用户名密码
        try {
            driver.findElement(By.id("username")).sendKeys("用户名"); // 用户名
            driver.findElement(By.id("password")).sendKeys("密码"); // 密码
            // 模拟点击
            driver.findElement(By.xpath("//form[@id='form-group-login']/button"))
                    .click(); // xpath语言：id为form-group-login的form下的button
        } catch (Exception e) {
            log.error("get username/password failed, but ingnore it for now", e);
        }

        // 防止页面未能及时加载出来而设置一段时间延迟
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Failed to pause thread", e);
        }

        // 获取cookie信息
        cookies = driver.manage().getCookies();
        driver.close();
    }
}