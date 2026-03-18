package com.fandesunstar.eship.service.pdf;

import com.fandesunstar.eship.entity.Contract;
import com.fandesunstar.eship.entity.Ship;
import com.fandesunstar.eship.entity.User;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.font.FontProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 合约PDF生成服务
 */
@Slf4j
@Service
public class ContractPdfService {

    /**
     * 生成合约PDF
     */
    public byte[] generateContractPdf(Contract contract, Ship ship, User owner, User renter) throws IOException {
        log.info("开始生成合约PDF：合约ID={}", contract.getId());

        // 生成HTML内容
        String htmlContent = generateContractHtml(contract, ship, owner, renter);

        // 转换为PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);

            // 配置中文字体
            ConverterProperties converterProperties = new ConverterProperties();
            FontProvider fontProvider = new FontProvider();

            // 添加中文字体支持
            fontProvider.addStandardPdfFonts();
            fontProvider.addSystemFonts();

            converterProperties.setFontProvider(fontProvider);

            // HTML转PDF
            HtmlConverter.convertToPdf(htmlContent, pdfDocument, converterProperties);

            log.info("合约PDF生成成功：合约ID=", contract.getId());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("生成合约PDF失败：合约ID={}", contract.getId(), e);
            throw new IOException("生成合约PDF失败", e);
        }
    }

    /**
     * 生成合约HTML内容
     */
    private String generateContractHtml(Contract contract, Ship ship, User owner, User renter) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

        // 计算租期天数
        long days = ChronoUnit.DAYS.between(contract.getStartDate(), contract.getEndDate()) + 1;

        // 生成合约编号
        String contractNo = "ES-" + contract.getId() + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'/>");
        html.append("<style>");
        html.append("body { font-family: 'SimSun', serif; font-size: 12pt; line-height: 1.8; margin: 40px; }");
        html.append("h1 { text-align: center; font-size: 20pt; margin-bottom: 30px; }");
        html.append("h2 { font-size: 14pt; margin-top: 20px; margin-bottom: 10px; }");
        html.append(".contract-no { text-align: right; font-size: 10pt; color: #666; margin-bottom: 20px; }");
        html.append(".party { margin: 15px 0; white-space: nowrap; }");
        html.append(".party-label { font-weight: bold; display: inline-block; white-space: nowrap; }");
        html.append(".section { margin: 20px 0; }");
        html.append(".clause { margin: 10px 0 10px 20px; }");
        html.append(".signature { margin-top: 50px; }");
        html.append(".signature-box { display: inline-block; width: 45%; margin: 20px 2%; }");
        html.append(".footer { margin-top: 50px; text-align: center; font-size: 10pt; color: #666; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 15px 0; }");
        html.append("td { padding: 8px; border: 1px solid #ddd; }");
        html.append(".label { background-color: #f5f5f5; font-weight: bold; width: 30%; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        // 标题
        html.append("<h1>船舶租赁合同</h1>");
        html.append("<div class='contract-no'>合同编号：").append(contractNo).append("</div>");

        // 甲乙双方
        html.append("<div class='section'>");
        html.append("<div class='party'>");
        html.append("<span class='party-label'>甲方（出租方）：</span>");
        html.append("<span style='white-space: nowrap;'>").append(owner.getUsername());
        if (owner.getCompanyName() != null && !owner.getCompanyName().isEmpty()) {
            html.append("（").append(owner.getCompanyName()).append("）");
        }
        html.append("</span>");
        html.append("</div>");
        html.append("<div class='party'>");
        html.append("<span class='party-label'>联系邮箱：</span>").append(owner.getEmail());
        html.append("</div>");
        html.append("<div class='party'>");
        html.append("<span class='party-label'>信用评分：</span>").append(owner.getCreditScore()).append(" 分");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='section'>");
        html.append("<div class='party'>");
        html.append("<span class='party-label'>乙方（承租方）：</span>");
        html.append("<span style='white-space: nowrap;'>").append(renter.getUsername());
        if (renter.getCompanyName() != null && !renter.getCompanyName().isEmpty()) {
            html.append("（").append(renter.getCompanyName()).append("）");
        }
        html.append("</span>");
        html.append("</div>");
        html.append("<div class='party'>");
        html.append("<span class='party-label'>联系邮箱：</span>").append(renter.getEmail());
        html.append("</div>");
        html.append("<div class='party'>");
        html.append("<span class='party-label'>信用评分：</span>").append(renter.getCreditScore()).append(" 分");
        html.append("</div>");
        html.append("</div>");

        // 船舶信息
        html.append("<h2>一、船舶信息</h2>");
        html.append("<table>");
        html.append("<tr><td class='label'>船舶名称</td><td>").append(ship.getShipName()).append("</td></tr>");
        html.append("<tr><td class='label'>船舶类型</td><td>").append(getShipTypeText(ship.getShipType())).append("</td></tr>");
        html.append("<tr><td class='label'>载重吨位</td><td>").append(ship.getTonnage()).append(" 吨</td></tr>");
        html.append("<tr><td class='label'>建造年份</td><td>").append(ship.getBuildYear()).append(" 年</td></tr>");
        html.append("<tr><td class='label'>船级社认证</td><td>").append(ship.getClassificationSociety()).append("</td></tr>");
        html.append("</table>");

        // 租赁条款
        html.append("<h2>二、租赁条款</h2>");
        html.append("<div class='clause'>");
        html.append("<strong>2.1 租赁期限：</strong>自 ").append(contract.getStartDate().format(formatter));
        html.append(" 起至 ").append(contract.getEndDate().format(formatter));
        html.append(" 止，共计 ").append(days).append(" 天。");
        html.append("</div>");

        html.append("<div class='clause'>");
        html.append("<strong>2.2 租金标准：</strong>日租金为人民币 ").append(String.format("%.2f", contract.getDailyRent()));
        html.append(" 元整（¥").append(String.format("%.2f", contract.getDailyRent())).append("）。");
        html.append("</div>");

        html.append("<div class='clause'>");
        html.append("<strong>2.3 租金总额：</strong>租赁期内租金总额为人民币 ");
        html.append(String.format("%.2f", contract.getTotalAmount()));
        html.append(" 元整（¥").append(String.format("%.2f", contract.getTotalAmount())).append("）。");
        html.append("</div>");

        html.append("<div class='clause'>");
        html.append("<strong>2.4 租赁用途：</strong>").append(contract.getPurpose());
        html.append("</div>");

        if (contract.getSpecialRequirements() != null && !contract.getSpecialRequirements().isEmpty()) {
            html.append("<div class='clause'>");
            html.append("<strong>2.5 特殊要求：</strong>").append(contract.getSpecialRequirements());
            html.append("</div>");
        }

        // 甲方权利义务
        html.append("<h2>三、甲方权利与义务</h2>");
        html.append("<div class='clause'>");
        html.append("<strong>3.1</strong> 甲方应保证船舶符合约定的技术标准和适航条件。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>3.2</strong> 甲方应在租赁期开始前将船舶交付乙方使用。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>3.3</strong> 甲方有权按约定收取租金，并监督船舶的使用情况。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>3.4</strong> 甲方应提供必要的船舶技术资料和操作说明。");
        html.append("</div>");

        // 乙方权利义务
        html.append("<h2>四、乙方权利与义务</h2>");
        html.append("<div class='clause'>");
        html.append("<strong>4.1</strong> 乙方应按约定用途使用船舶，不得擅自改变用途。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>4.2</strong> 乙方应按时支付租金，不得拖欠。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>4.3</strong> 乙方应妥善保管和维护船舶，保持船舶良好状态。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>4.4</strong> 租赁期满后，乙方应按时归还船舶。");
        html.append("</div>");

        // 违约责任
        html.append("<h2>五、违约责任</h2>");
        html.append("<div class='clause'>");
        html.append("<strong>5.1</strong> 任何一方违反本合同约定，应承担违约责任，赔偿对方因此遭受的损失。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>5.2</strong> 乙方逾期支付租金的，每逾期一日，应按欠付租金的 3% 支付违约金。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>5.3</strong> 乙方擅自改变船舶用途或损坏船舶的，应承担修复费用并支付违约金。");
        html.append("</div>");

        // 争议解决
        html.append("<h2>六、争议解决</h2>");
        html.append("<div class='clause'>");
        html.append("<strong>6.1</strong> 本合同履行过程中发生的争议，双方应友好协商解决。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>6.2</strong> 协商不成的，可向 E-ship 平台申请调解或提交仲裁机构仲裁。");
        html.append("</div>");

        // 其他条款
        html.append("<h2>七、其他条款</h2>");
        html.append("<div class='clause'>");
        html.append("<strong>7.1</strong> 本合同自双方签字（电子签章）之日起生效。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>7.2</strong> 本合同一式两份，甲乙双方各执一份，具有同等法律效力。");
        html.append("</div>");
        html.append("<div class='clause'>");
        html.append("<strong>7.3</strong> 本合同未尽事宜，双方可另行协商补充。");
        html.append("</div>");

        // 签署信息
        html.append("<div class='signature'>");
        html.append("<div class='signature-box'>");
        html.append("<p><strong>甲方（出租方）：</strong>").append(owner.getUsername()).append("</p>");
        html.append("<p>签署日期：").append(LocalDate.now().format(formatter)).append("</p>");
        html.append("<p>电子签章：[已签署]</p>");
        html.append("</div>");

        html.append("<div class='signature-box'>");
        html.append("<p><strong>乙方（承租方）：</strong>").append(renter.getUsername()).append("</p>");
        html.append("<p>签署日期：").append(LocalDate.now().format(formatter)).append("</p>");
        html.append("<p>电子签章：[已签署]</p>");
        html.append("</div>");
        html.append("</div>");

        // 页脚
        html.append("<div class='footer'>");
        html.append("<p>本合同由 E-ship（船易达）智慧航运平台自动生成</p>");
        html.append("<p>平台网址：www.eship.com | 客服邮箱：188043648@qq.com</p>");
        html.append("<p>生成时间：").append(LocalDate.now().format(formatter)).append("</p>");
        html.append("</div>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * 获取船舶类型文本
     */
    private String getShipTypeText(String type) {
        return switch (type) {
            case "CONTAINER" -> "集装箱船";
            case "BULK" -> "散货船";
            case "TANKER" -> "油船";
            case "PASSENGER" -> "客船";
            default -> type;
        };
    }
}
