package com.example.onlinestore.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageResponse DTO 测试
 */
@DisplayName("分页响应DTO测试")
public class PageResponseTest {

    private PageResponse<String> pageResponse;

    @BeforeEach
    void setUp() {
        pageResponse = new PageResponse<>();
    }

    @Nested
    @DisplayName("Records字段测试")
    class RecordsTests {

        @Test
        @DisplayName("设置和获取记录列表")
        void testRecordsGetterSetter() {
            // Arrange
            List<String> records = Arrays.asList("item1", "item2", "item3");

            // Act
            pageResponse.setRecords(records);

            // Assert
            assertEquals(records, pageResponse.getRecords());
            assertEquals(3, pageResponse.getRecords().size());
        }

        @Test
        @DisplayName("设置null记录列表")
        void testSetNullRecords() {
            // Act
            pageResponse.setRecords(null);

            // Assert
            assertNull(pageResponse.getRecords());
        }

        @Test
        @DisplayName("设置空记录列表")
        void testSetEmptyRecords() {
            // Arrange
            List<String> emptyRecords = new ArrayList<>();

            // Act
            pageResponse.setRecords(emptyRecords);

            // Assert
            assertEquals(emptyRecords, pageResponse.getRecords());
            assertTrue(pageResponse.getRecords().isEmpty());
        }

        @Test
        @DisplayName("设置单个记录的列表")
        void testSetSingleRecord() {
            // Arrange
            List<String> singleRecord = Collections.singletonList("onlyitem");

            // Act
            pageResponse.setRecords(singleRecord);

            // Assert
            assertEquals(singleRecord, pageResponse.getRecords());
            assertEquals(1, pageResponse.getRecords().size());
            assertEquals("onlyitem", pageResponse.getRecords().get(0));
        }

        @Test
        @DisplayName("设置大量记录的列表")
        void testSetLargeRecordsList() {
            // Arrange
            List<String> largeRecords = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                largeRecords.add("item" + i);
            }

            // Act
            pageResponse.setRecords(largeRecords);

            // Assert
            assertEquals(largeRecords, pageResponse.getRecords());
            assertEquals(1000, pageResponse.getRecords().size());
        }
    }

    @Nested
    @DisplayName("Total字段测试")
    class TotalTests {

        @Test
        @DisplayName("设置和获取总数")
        void testTotalGetterSetter() {
            // Arrange
            long total = 100L;

            // Act
            pageResponse.setTotal(total);

            // Assert
            assertEquals(total, pageResponse.getTotal());
        }

        @Test
        @DisplayName("设置零总数")
        void testSetZeroTotal() {
            // Arrange
            long zeroTotal = 0L;

            // Act
            pageResponse.setTotal(zeroTotal);

            // Assert
            assertEquals(zeroTotal, pageResponse.getTotal());
        }

        @Test
        @DisplayName("设置负数总数")
        void testSetNegativeTotal() {
            // Arrange
            long negativeTotal = -1L;

            // Act
            pageResponse.setTotal(negativeTotal);

            // Assert
            assertEquals(negativeTotal, pageResponse.getTotal());
        }

        @Test
        @DisplayName("设置大数值总数")
        void testSetLargeTotal() {
            // Arrange
            long largeTotal = Long.MAX_VALUE;

            // Act
            pageResponse.setTotal(largeTotal);

            // Assert
            assertEquals(largeTotal, pageResponse.getTotal());
        }
    }

    @Nested
    @DisplayName("PageNum字段测试")
    class PageNumTests {

        @Test
        @DisplayName("设置和获取页码")
        void testPageNumGetterSetter() {
            // Arrange
            int pageNum = 5;

            // Act
            pageResponse.setPageNum(pageNum);

            // Assert
            assertEquals(pageNum, pageResponse.getPageNum());
        }

        @Test
        @DisplayName("设置第一页页码")
        void testSetFirstPageNum() {
            // Arrange
            int firstPage = 1;

            // Act
            pageResponse.setPageNum(firstPage);

            // Assert
            assertEquals(firstPage, pageResponse.getPageNum());
        }

        @Test
        @DisplayName("设置零页码")
        void testSetZeroPageNum() {
            // Arrange
            int zeroPage = 0;

            // Act
            pageResponse.setPageNum(zeroPage);

            // Assert
            assertEquals(zeroPage, pageResponse.getPageNum());
        }

        @Test
        @DisplayName("设置负数页码")
        void testSetNegativePageNum() {
            // Arrange
            int negativePage = -1;

            // Act
            pageResponse.setPageNum(negativePage);

            // Assert
            assertEquals(negativePage, pageResponse.getPageNum());
        }

        @Test
        @DisplayName("设置大数值页码")
        void testSetLargePageNum() {
            // Arrange
            int largePage = Integer.MAX_VALUE;

            // Act
            pageResponse.setPageNum(largePage);

            // Assert
            assertEquals(largePage, pageResponse.getPageNum());
        }
    }

    @Nested
    @DisplayName("PageSize字段测试")
    class PageSizeTests {

        @Test
        @DisplayName("设置和获取页大小")
        void testPageSizeGetterSetter() {
            // Arrange
            int pageSize = 10;

            // Act
            pageResponse.setPageSize(pageSize);

            // Assert
            assertEquals(pageSize, pageResponse.getPageSize());
        }

        @Test
        @DisplayName("设置常见页大小值")
        void testSetCommonPageSizes() {
            // Arrange & Act & Assert
            int[] commonSizes = {10, 20, 50, 100};
            
            for (int size : commonSizes) {
                pageResponse.setPageSize(size);
                assertEquals(size, pageResponse.getPageSize());
            }
        }

        @Test
        @DisplayName("设置零页大小")
        void testSetZeroPageSize() {
            // Arrange
            int zeroSize = 0;

            // Act
            pageResponse.setPageSize(zeroSize);

            // Assert
            assertEquals(zeroSize, pageResponse.getPageSize());
        }

        @Test
        @DisplayName("设置负数页大小")
        void testSetNegativePageSize() {
            // Arrange
            int negativeSize = -1;

            // Act
            pageResponse.setPageSize(negativeSize);

            // Assert
            assertEquals(negativeSize, pageResponse.getPageSize());
        }

        @Test
        @DisplayName("设置大数值页大小")
        void testSetLargePageSize() {
            // Arrange
            int largeSize = 1000;

            // Act
            pageResponse.setPageSize(largeSize);

            // Assert
            assertEquals(largeSize, pageResponse.getPageSize());
        }
    }

    @Nested
    @DisplayName("泛型类型测试")
    class GenericTypeTests {

        @Test
        @DisplayName("String类型的分页响应")
        void testStringPageResponse() {
            // Arrange
            PageResponse<String> stringResponse = new PageResponse<>();
            List<String> stringRecords = Arrays.asList("a", "b", "c");

            // Act
            stringResponse.setRecords(stringRecords);
            stringResponse.setTotal(3L);
            stringResponse.setPageNum(1);
            stringResponse.setPageSize(10);

            // Assert
            assertEquals(stringRecords, stringResponse.getRecords());
            assertEquals(3L, stringResponse.getTotal());
            assertEquals(1, stringResponse.getPageNum());
            assertEquals(10, stringResponse.getPageSize());
        }

        @Test
        @DisplayName("Integer类型的分页响应")
        void testIntegerPageResponse() {
            // Arrange
            PageResponse<Integer> integerResponse = new PageResponse<>();
            List<Integer> integerRecords = Arrays.asList(1, 2, 3);

            // Act
            integerResponse.setRecords(integerRecords);
            integerResponse.setTotal(3L);
            integerResponse.setPageNum(1);
            integerResponse.setPageSize(10);

            // Assert
            assertEquals(integerRecords, integerResponse.getRecords());
            assertEquals(3L, integerResponse.getTotal());
        }

        @Test
        @DisplayName("UserVO类型的分页响应")
        void testUserVOPageResponse() {
            // Arrange
            PageResponse<UserVO> userVOResponse = new PageResponse<>();
            UserVO user1 = new UserVO();
            user1.setId(1L);
            user1.setUsername("user1");
            
            UserVO user2 = new UserVO();
            user2.setId(2L);
            user2.setUsername("user2");
            
            List<UserVO> userRecords = Arrays.asList(user1, user2);

            // Act
            userVOResponse.setRecords(userRecords);
            userVOResponse.setTotal(2L);
            userVOResponse.setPageNum(1);
            userVOResponse.setPageSize(10);

            // Assert
            assertEquals(userRecords, userVOResponse.getRecords());
            assertEquals(2, userVOResponse.getRecords().size());
            assertEquals("user1", userVOResponse.getRecords().get(0).getUsername());
            assertEquals("user2", userVOResponse.getRecords().get(1).getUsername());
        }
    }

    @Nested
    @DisplayName("对象完整性测试")
    class ObjectIntegrityTests {

        @Test
        @DisplayName("创建完整的分页响应对象")
        void testCompletePageResponse() {
            // Arrange
            List<String> records = Arrays.asList("item1", "item2", "item3");
            long total = 100L;
            int pageNum = 2;
            int pageSize = 10;

            // Act
            pageResponse.setRecords(records);
            pageResponse.setTotal(total);
            pageResponse.setPageNum(pageNum);
            pageResponse.setPageSize(pageSize);

            // Assert
            assertEquals(records, pageResponse.getRecords());
            assertEquals(total, pageResponse.getTotal());
            assertEquals(pageNum, pageResponse.getPageNum());
            assertEquals(pageSize, pageResponse.getPageSize());
        }

        @Test
        @DisplayName("新创建的对象默认值")
        void testNewObjectDefaultValues() {
            // Arrange
            PageResponse<String> newResponse = new PageResponse<>();

            // Assert
            assertNull(newResponse.getRecords());
            assertEquals(0L, newResponse.getTotal());
            assertEquals(0, newResponse.getPageNum());
            assertEquals(0, newResponse.getPageSize());
        }

        @Test
        @DisplayName("测试对象字段独立性")
        void testFieldIndependence() {
            // Arrange
            List<String> records1 = Arrays.asList("a", "b");
            List<String> records2 = Arrays.asList("c", "d");
            long total1 = 50L;
            long total2 = 100L;

            // Act & Assert
            pageResponse.setRecords(records1);
            pageResponse.setTotal(total1);
            assertEquals(records1, pageResponse.getRecords());
            assertEquals(total1, pageResponse.getTotal());

            pageResponse.setRecords(records2);
            assertEquals(records2, pageResponse.getRecords());
            assertEquals(total1, pageResponse.getTotal()); // total should remain unchanged

            pageResponse.setTotal(total2);
            assertEquals(records2, pageResponse.getRecords()); // records should remain unchanged
            assertEquals(total2, pageResponse.getTotal());
        }
    }

    @Nested
    @DisplayName("业务场景测试")
    class BusinessScenarioTests {

        @Test
        @DisplayName("第一页数据场景")
        void testFirstPageScenario() {
            // Arrange
            List<String> firstPageRecords = Arrays.asList("user1", "user2", "user3");
            long totalUsers = 25L;
            int pageNum = 1;
            int pageSize = 10;

            // Act
            pageResponse.setRecords(firstPageRecords);
            pageResponse.setTotal(totalUsers);
            pageResponse.setPageNum(pageNum);
            pageResponse.setPageSize(pageSize);

            // Assert
            assertEquals(firstPageRecords, pageResponse.getRecords());
            assertEquals(totalUsers, pageResponse.getTotal());
            assertEquals(pageNum, pageResponse.getPageNum());
            assertEquals(pageSize, pageResponse.getPageSize());
            assertEquals(3, pageResponse.getRecords().size()); // 当前页实际记录数
        }

        @Test
        @DisplayName("最后一页数据场景")
        void testLastPageScenario() {
            // Arrange - 总共25条记录，每页10条，第3页只有5条记录
            List<String> lastPageRecords = Arrays.asList("user21", "user22", "user23", "user24", "user25");
            long totalUsers = 25L;
            int pageNum = 3;
            int pageSize = 10;

            // Act
            pageResponse.setRecords(lastPageRecords);
            pageResponse.setTotal(totalUsers);
            pageResponse.setPageNum(pageNum);
            pageResponse.setPageSize(pageSize);

            // Assert
            assertEquals(lastPageRecords, pageResponse.getRecords());
            assertEquals(totalUsers, pageResponse.getTotal());
            assertEquals(pageNum, pageResponse.getPageNum());
            assertEquals(pageSize, pageResponse.getPageSize());
            assertEquals(5, pageResponse.getRecords().size()); // 最后一页只有5条记录
            assertTrue(pageResponse.getRecords().size() < pageResponse.getPageSize());
        }

        @Test
        @DisplayName("空结果集场景")
        void testEmptyResultScenario() {
            // Arrange
            List<String> emptyRecords = new ArrayList<>();
            long totalUsers = 0L;
            int pageNum = 1;
            int pageSize = 10;

            // Act
            pageResponse.setRecords(emptyRecords);
            pageResponse.setTotal(totalUsers);
            pageResponse.setPageNum(pageNum);
            pageResponse.setPageSize(pageSize);

            // Assert
            assertEquals(emptyRecords, pageResponse.getRecords());
            assertEquals(totalUsers, pageResponse.getTotal());
            assertEquals(pageNum, pageResponse.getPageNum());
            assertEquals(pageSize, pageResponse.getPageSize());
            assertTrue(pageResponse.getRecords().isEmpty());
            assertEquals(0L, pageResponse.getTotal());
        }

        @Test
        @DisplayName("单页全部数据场景")
        void testSinglePageAllDataScenario() {
            // Arrange - 总共只有3条记录，一页就能显示完
            List<String> allRecords = Arrays.asList("user1", "user2", "user3");
            long totalUsers = 3L;
            int pageNum = 1;
            int pageSize = 10;

            // Act
            pageResponse.setRecords(allRecords);
            pageResponse.setTotal(totalUsers);
            pageResponse.setPageNum(pageNum);
            pageResponse.setPageSize(pageSize);

            // Assert
            assertEquals(allRecords, pageResponse.getRecords());
            assertEquals(totalUsers, pageResponse.getTotal());
            assertEquals(pageNum, pageResponse.getPageNum());
            assertEquals(pageSize, pageResponse.getPageSize());
            assertEquals(3, pageResponse.getRecords().size());
            assertTrue(pageResponse.getRecords().size() < pageResponse.getPageSize());
            assertEquals(pageResponse.getTotal(), pageResponse.getRecords().size());
        }

        @Test
        @DisplayName("满页数据场景")
        void testFullPageScenario() {
            // Arrange - 一页正好10条记录
            List<String> fullPageRecords = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                fullPageRecords.add("user" + i);
            }
            long totalUsers = 50L;
            int pageNum = 2;
            int pageSize = 10;

            // Act
            pageResponse.setRecords(fullPageRecords);
            pageResponse.setTotal(totalUsers);
            pageResponse.setPageNum(pageNum);
            pageResponse.setPageSize(pageSize);

            // Assert
            assertEquals(fullPageRecords, pageResponse.getRecords());
            assertEquals(totalUsers, pageResponse.getTotal());
            assertEquals(pageNum, pageResponse.getPageNum());
            assertEquals(pageSize, pageResponse.getPageSize());
            assertEquals(10, pageResponse.getRecords().size());
            assertEquals(pageResponse.getPageSize(), pageResponse.getRecords().size());
        }
    }
}