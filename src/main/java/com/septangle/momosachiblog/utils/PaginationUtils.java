package com.septangle.momosachiblog.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public class PaginationUtils {
    public static <T>Page<T> getByRecords(List<T> records, int current, int size) {
        Page<T> page = new Page<>(current, size);

        // 计算当前页的位置
        int total = records.size();
        int fromIndex = Math.min((current - 1) * size, total);
        int toIndex = Math.min(current * size, total);

        page.setRecords(records.subList(fromIndex, toIndex));
        page.setTotal(total);
        return page;
    }
}
