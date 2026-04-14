package org.example.vgtuaventory.requirementsBasedTesting;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class A3_NR1_TS_1 {

    private static final DateTimeFormatter SCREENSHOT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    WebDriver driver = new ChromeDriver();

    @Test
    public void correctDataInput(){
        try {

            driver.get("http://localhost:3000");
            driver.findElement(By.id("start-date")).sendKeys("02022025");
            driver.findElement(By.id("end-date")).sendKeys("03062026");
            driver.findElement(By.xpath("//button[@type='submit']")).click();
            String totalProfit = driver.findElement(By.id("total-profit")).getText();
            assertTrue(totalProfit.matches("\\d+\\.\\d{2}"));
            saveScreenshot(driver, "passed");
        } catch (Throwable throwable) {
            if (driver != null) {
                saveScreenshot(driver, "failed");
            }
            throw throwable;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Test
    public void endDateChosenInTheFuture() {
        try {
            driver = new ChromeDriver();
            driver.get("http://localhost:3000");
            driver.findElement(By.id("end-date")).sendKeys("03062030");
            assertEquals("End date cannot be in the future.", driver.findElement(By.id("error-message")).getText());
            saveScreenshot(driver, "passed");
        } catch (Throwable throwable) {
            if (driver != null) {
                saveScreenshot(driver, "failed");
            }
            throw throwable;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Test
    public void startDateLaterThanEndDate() {
        try {
            driver = new ChromeDriver();
            driver.get("http://localhost:3000");
            driver.findElement(By.id("end-date")).sendKeys("02022025");
            driver.findElement(By.id("start-date")).sendKeys("03062026");
            assertEquals("Start date cannot be later than end date.", driver.findElement(By.id("error-message")).getText());
            saveScreenshot(driver, "passed");
        } catch (Throwable throwable) {
            if (driver != null) {
                saveScreenshot(driver, "failed");
            }
            throw throwable;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Test
    public void startDateChosenSoonerThanFirstOrder() {
        try {
            driver = new ChromeDriver();
            driver.get("http://localhost:3000");
            driver.findElement(By.id("start-date")).sendKeys("03062005");
            assertEquals("Start date cannot be sooner than first recorded sale.", driver.findElement(By.id("error-message")).getText());
            saveScreenshot(driver, "passed");
        } catch (Throwable throwable) {
            if (driver != null) {
                saveScreenshot(driver, "failed");
            }
            throw throwable;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void saveScreenshot(WebDriver driver, String status) {
        if (!(driver instanceof TakesScreenshot)) {
            return;
        }

        try {
            Path screenshotDir = Paths.get("build", "test-screenshots");
            Files.createDirectories(screenshotDir);

            String fileName = "A3_NR1_TS_1" + status + "_" + LocalDateTime.now().format(SCREENSHOT_TIMESTAMP) + ".png";
            Path target = screenshotDir.resolve(fileName);
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(source.toPath(), target);
        } catch (IOException ignored) {
            // Screenshot capture should not change the test result.
        }
    }

}
