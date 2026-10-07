package com.ticketgen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebUiIT {
    private static final Path DATA;
    static {
        try {
            DATA = Files.createTempDirectory("ticketgen-selenium-");
            TestFiles.students(DATA);
            TestFiles.tickets(DATA);
        } catch (Exception error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    @DynamicPropertySource
    static void dataDirectory(DynamicPropertyRegistry registry) {
        registry.add("ticketgen.data-directory", DATA::toString);
    }

    @LocalServerPort int port;
    private WebDriver driver;

    @BeforeAll
    static void verifyIsolatedData() {
        assertTrue(DATA.startsWith(Path.of(System.getProperty("java.io.tmpdir"))));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) driver.quit();
    }

    @Test
    void browserFlowIncludesSelectionGenerationEscRepeatAndEmptyGroup() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
                "--window-size=1280,900");
        driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get("http://localhost:" + port);

        var group = driver.findElement(By.id("group"));
        var student = driver.findElement(By.id("student"));
        var button = driver.findElement(By.id("generate"));
        assertFalse(student.isEnabled());
        assertFalse(button.isEnabled());

        capture("main-page.png");

        new Select(group).selectByVisibleText("TI-221");
        wait.until(d -> d.findElements(By.cssSelector("#student option")).size() == 2);
        assertTrue(student.isEnabled());
        assertEquals("Ivanov Ivan", new Select(student).getOptions().get(1).getText());
        assertFalse(button.isEnabled());

        new Select(student).selectByVisibleText("Ivanov Ivan");
        wait.until(d -> button.isEnabled());
        button.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result-dialog")));
        int firstNumber = Integer.parseInt(driver.findElement(By.id("ticket-number")).getText());
        assertTrue(firstNumber == 7 || firstNumber == 15);
        assertEquals(3, driver.findElements(By.cssSelector("#questions li")).size());
        assertTrue(driver.findElement(By.id("result-title")).getText().contains("Ваш билет №"));
        capture("ticket-result.png");

        driver.findElement(By.id("result-dialog")).sendKeys(Keys.ESCAPE);
        wait.until(d -> !driver.findElement(By.id("result-dialog")).isDisplayed());
        assertTrue(driver.findElement(By.id("group")).isDisplayed());
        assertFalse(driver.findElement(By.id("generate")).getAttribute("disabled") != null);

        button.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result-dialog")));
        assertEquals(firstNumber, Integer.parseInt(driver.findElement(By.id("ticket-number")).getText()));
        driver.findElement(By.id("close-result")).click();
        wait.until(d -> !driver.findElement(By.id("result-dialog")).isDisplayed());

        try (var input = Files.newInputStream(DATA.resolve("results.xlsx"));
             var book = new org.apache.poi.xssf.usermodel.XSSFWorkbook(input)) {
            var sheet = book.getSheetAt(0);
            assertEquals(2, sheet.getLastRowNum());
            assertEquals("нет", sheet.getRow(1).getCell(5).getStringCellValue());
            assertEquals("да", sheet.getRow(2).getCell(5).getStringCellValue());
        }

        new Select(group).selectByVisibleText("EMPTY");
        wait.until(d -> driver.findElement(By.id("status")).getText().contains("нет студентов"));
        assertFalse(student.isEnabled());
        assertFalse(button.isEnabled());

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            new Select(group).selectByVisibleText("TI-221");
            wait.until(d -> d.findElements(By.cssSelector("#student option")).size() == 2);
            new Select(student).selectByVisibleText("Ivanov Ivan");
            wait.until(d -> button.isEnabled());
            try (FileChannel channel = FileChannel.open(DATA.resolve("results.xlsx"),
                    StandardOpenOption.READ, StandardOpenOption.WRITE);
                 FileLock ignored = channel.lock()) {
                button.click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lock-dialog")));
                assertFalse(driver.findElement(By.id("result-dialog")).isDisplayed());
                assertTrue(driver.findElement(By.id("retry")).isDisplayed());
                assertTrue(driver.findElement(By.id("return")).isDisplayed());
                capture("file-locked.png");
            }
            driver.findElement(By.id("retry")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result-dialog")));
            assertEquals(firstNumber, Integer.parseInt(driver.findElement(By.id("ticket-number")).getText()));
            try (var input = Files.newInputStream(DATA.resolve("results.xlsx"));
                 var book = new org.apache.poi.xssf.usermodel.XSSFWorkbook(input)) {
                assertEquals(3, book.getSheetAt(0).getLastRowNum());
            }
        }
    }

    private void capture(String name) throws Exception {
        if (Boolean.getBoolean("ticketgen.capture-screenshots")) {
            Path destination = Path.of("docs", "screenshots", name);
            Files.createDirectories(destination.getParent());
            Files.copy(((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath(),
                    destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
