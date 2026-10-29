package cn.iocoder.yudao.framework.excel.core.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteTableHolder;
import cn.iocoder.yudao.framework.common.util.MessageUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Locale;

/**
 * FastExcel 表头国际化 CellWriteHandler
 */
public class I18nCellWriteHandler implements CellWriteHandler {

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head,
                                 Integer relativeRowIndex, Boolean isHead) {
        // 只拦截表头单元格
        if (!Boolean.TRUE.equals(isHead) || head == null) {
            return;
        }



        // 获取表头绑定的 Message Key 列表
        List<String> headNameList = head.getHeadNameList();
        if (headNameList != null && !headNameList.isEmpty()) {
            // 获取当前行对应的 Key (支持多层复合表头)
            int index = relativeRowIndex != null ? relativeRowIndex : 0;
            if (index < headNameList.size()) {
                String originalKey = headNameList.get(index);
                if (originalKey != null && !originalKey.isBlank()) {
                    if(originalKey.startsWith("${")) {
                        originalKey=originalKey.replace("${","").replace("}","");
                        // 翻译并替换单元格中的值
                        String translated = MessageUtils.message(originalKey);
                        cell.setCellValue(translated);
                    }else {
                        cell.setCellValue(originalKey);
                    }

                }
            }
        }
    }
}