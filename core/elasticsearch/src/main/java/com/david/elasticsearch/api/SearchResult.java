package com.david.elasticsearch.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@Builder
public class SearchResult<T> implements Serializable {

    private Integer pageNum;//页码
    private Integer pageSize;//每页显示大小
    private long total;//总数
    private int pages;//总页数
    private int[] pageNums;//导航
    private Integer navigatePages = 5;//导航页码数
    private Integer skip;//跳过记录条数
    private List<T> result;//查询结果
    private Integer count;//当前页数量

    public SearchResult(Integer pageNum, Integer pageSize, long total) {
        if (pageNum == null || pageNum < 1) {
            this.pageNum = 1;
        } else {
            this.pageNum = pageNum;
        }
        if (pageSize == null || pageSize < 0) {
            this.pageSize = 10;
        } else {
            this.pageSize = pageSize;
        }
        this.total = total;
        //计算总页数
        this.pages = (int) (total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
        this.skip = (pageNum - 1) * pageSize;
        calcPageNums();
        //计算count
        this.count = Math.toIntExact(total >= (this.skip + this.pageSize) ? pageSize : this.skip + this.pageSize - total);
        if (total < pageSize) {
            this.count = Math.toIntExact(total);
        } else if (pages == pageNum) {
            this.count = Math.toIntExact(this.total - this.skip);
        } else {
            this.count = this.pageSize;
        }
    }

    private void calcPageNums() {
        //当总页数小于或等于导航页码数时
        if (pages <= navigatePages) {
            pageNums = new int[pages];
            for (int i = 0; i < pages; i++) {
                pageNums[i] = i + 1;
            }
        } else { //当总页数大于导航页码数时
            pageNums = new int[navigatePages];
            int startNum = pageNum - navigatePages / 2;
            int endNum = pageNum + navigatePages / 2;

            if (startNum < 1) {
                startNum = 1;
                //(最前navigatePages页
                for (int i = 0; i < navigatePages; i++) {
                    pageNums[i] = startNum++;
                }
            } else if (endNum > pages) {
                endNum = pages;
                //最后navigatePages页
                for (int i = navigatePages - 1; i >= 0; i--) {
                    pageNums[i] = endNum--;
                }
            } else {
                //所有中间页
                for (int i = 0; i < navigatePages; i++) {
                    pageNums[i] = startNum++;
                }
            }
        }
    }

}
