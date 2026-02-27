package com.example.homic.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.20:15
 * 项目名：homic
 */
public class PageResultVO<T> {
        public static ModelMapper modelMapper = new ModelMapper();
        private Integer totalCount;
        private Integer pageSize;
        private Integer pageNo;
        private Integer pageTotal;
        private List<T> list;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(Integer pageTotal) {
        this.pageTotal = pageTotal;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }
     public PageResultVO(IPage<T> page, Class<T> targetClass) {
        //根据分页记录装填分页VO
        int totalCount = Integer.parseInt(String.valueOf(page.getTotal()));
        int pageTotal =Integer.parseInt(String.valueOf(page.getPages()));
        int pageNo = Integer.parseInt(String.valueOf(page.getCurrent()));
        int pageSize = Integer.parseInt(String.valueOf(page.getSize()));
        this.setPageNo(pageNo);//设置页码
        this.setPageTotal(pageTotal);//设置总页数
         this.setPageSize(pageSize);//设置单页记录数
         this.setTotalCount(totalCount);//设置总记录数
        List<T> resultList = page.getRecords().stream()
                .map(fileInfo -> modelMapper.map(fileInfo,targetClass))//map将元素经过函数处理得到新的Stream流
                .collect(Collectors.toList());//collect()用于将stream流转化成新的格式，而这个新格式是由collect()函数的参数决定
        this.setList(resultList);
    }
}
