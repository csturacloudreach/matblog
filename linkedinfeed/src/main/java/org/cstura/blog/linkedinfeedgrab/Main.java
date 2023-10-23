/**
 * 
 */
package org.cstura.blog.linkedinfeedgrab;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;

/**
 * 
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable {
		//extract my username and password from a local file if that exists, so we don't have it hardcoded in the codebase.
		final var defaultCredentialsFilePath = Path.of(System.getProperty("user.home"), ".config", "linkedin", "auth");
		File authFile = new File(args.length > 0 ? args[0] : defaultCredentialsFilePath.toString());
		if(!authFile.exists()) {
			System.err.println("Error, no authentication file with LinkedIn credentials exists, please specify a credentials file to use or store your credentials in "+defaultCredentialsFilePath.toAbsolutePath().toString());
			System.exit(-1);
		} 
		
		var authConfig = new Properties();
		authConfig.load(new FileInputStream(authFile));
		
		var liUsername = authConfig.getProperty("username");
		var liPassword = authConfig.getProperty("password");
		
		FirefoxOptions options = new FirefoxOptions();
		options.addArguments("-headless");
		options.setCapability("acceptInsecureCerts", true);
		options.setCapability("unhandledPromptBehavior", "accept");
		
		
		WebDriver driver = new FirefoxDriver(options);
		try {
			driver.get("https://www.linkedin.com/login?fromSignIn=true&trk=guest_homepage-basic_nav-header-signin");
			
			var username = driver.findElement(By.id("username"));
			var password = driver.findElement(By.id("password"));						
			
			username.click();
			username.sendKeys(liUsername); //I need to find a better way to provide my linkedIn credentials for use here.
			
			password.click();
			password.sendKeys(liPassword);
			
			var loginBtn = driver.findElement(By.xpath("//div/button[@type = 'submit'][text() = 'Sign in']"));
			loginBtn.click();						
			
			Thread.sleep(TimeUnit.SECONDS.toMillis(5));
			
			var pageTitle = driver.getTitle();
			if(pageTitle.endsWith("Feed | LinkedIn")) {
				//first we want to press the page down key 10 times, to load up a nice list of posts to capture from my feed.
				for(int i = 0; i < 10; i++) {
					new Actions(driver)
						.sendKeys(Keys.PAGE_DOWN)
						.build()
						.perform();
				}
				var posts = driver.findElements(By.className("feed-shared-update-v2"));
				posts.stream()
					.forEach(we -> {
						try {
							String whoStr = "";
							try {
								var who = we.findElement(By.xpath("div//div[contains(@class,'update-components-actor__meta')]/a[contains(@class,'update-components-actor__meta-link')]"));
								whoStr = who.getAttribute("aria-label").substring(6);
							}catch(Throwable ex) {}
							var what = we.findElement(By.xpath("div//div[contains(@class,'feed-shared-update-v2__description-wrapper')]//div[@dir = 'ltr']//span[@dir = 'ltr']"));
							var postId = we.findElement(By.tagName("h2"));							
							if(!whoStr.isEmpty()) {
								System.out.println("Who: "+whoStr);
							}
							System.out.println("What: "+what.getText());
						}catch(Throwable ex) {
							System.out.println("I was unable to parse the feed post because of the error: "+ex.getMessage());
						}
					});
			} else {
				throw new Exception("Error, I'm on the wrong page, I've landed on a page with the title of: "+pageTitle+" which is not my feed");
			}
		} finally {
			driver.close();
			System.exit(0);
		}
	}

}
