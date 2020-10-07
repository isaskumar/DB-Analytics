import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.github.bonigarcia.wdm.WebDriverManager;

public class DBTest {

	WebDriver driver;
	MongoCollection<Document> dataCollection;

	@BeforeSuite()
	public void connectDataBase() {
		Logger mongoDBLogger = Logger.getLogger("org.mongodb.driver");
		MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
		//Get the database 
		MongoDatabase database = mongoClient.getDatabase("testDB");
		//Create collection to the database
		dataCollection = database.getCollection("data");
	}


	@BeforeTest()
	public void setUp() {
		WebDriverManager.chromedriver().setup();
		ChromeOptions co = new ChromeOptions();
		co.addArguments("--headless");
		driver = new ChromeDriver(co);	
	}

	@DataProvider
	public Object[][] getData() {
		return new Object[][] {
			{"https://www.amazon.it/"},
			{"https://www.zalando.it/"},
			{"https://www.moncler.com/it"}
		};
	}

	@Test(dataProvider="getData")
	public void webTest(String appUrl) {
		driver.get(appUrl);
		String url = driver.getCurrentUrl();
		String title = driver.getTitle();
		int linksCount = driver.findElements(By.tagName("a")).size();
		int imagesCount = driver.findElements(By.tagName("img")).size();

		List<WebElement> linksList = driver.findElements(By.tagName("a"));
		List<String> linksArrayList = new ArrayList<String>();
		
		List<WebElement> imagesList = driver.findElements(By.tagName("img"));
		List<String> imagesArrayList = new ArrayList<String>();
		Document d1 = new Document();
		d1.append("url", url);
		d1.append("title", title);
		d1.append("totalLinks", linksCount);
		d1.append("totalImages", imagesCount);

		for(WebElement ele: linksList) {
			String hrefValue = ele.getAttribute("href");
			linksArrayList.add(hrefValue);
		}
		
		for(WebElement ele: imagesList) {
			String srcValue = ele.getAttribute("src");
			imagesArrayList.add(srcValue);
		}

		d1.append("linksAttribute", linksArrayList);

		List<Document> docs = new ArrayList<Document>();
		docs.add(d1);
		dataCollection.insertMany(docs);
	}

	@AfterTest()
	public void tearDown() {
		driver.quit();
	}
}
