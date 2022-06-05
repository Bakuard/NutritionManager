package com.bakuard.nutritionManager.service.report;

import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;

import net.sf.jasperreports.engine.*;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.bakuard.nutritionManager.validation.Rule.failure;

public class ReportService {

    private List<JasperReport> reportTemplates;

    public ReportService() {

    }

    public void loadTemplates() {
        try {
            reportTemplates = List.of(
                    loadTemplate("reportTemplates/dishProductsReportTemplate.jrxml"),
                    loadTemplate("reportTemplates/menuProductsReportTemplate.jrxml")
            );
        } catch(IOException | JRException e) {
            throw new RuntimeException("Fail to load report templates", e);
        }
    }

    public record DishProductsReportData(Dish dish,
                                         BigDecimal servingNumber,
                                         List<Dish.ProductConstraint> constraints,
                                         Locale locale) {}

    public record MenuProductsReportData(Menu menu,
                                         BigDecimal menuNumber,
                                         List<Menu.ProductConstraint> constraints,
                                         Locale locale) {}

    public byte[] createDishProductsReport(DishProductsReportData data) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(JRParameter.REPORT_LOCALE, data.locale());

        try {
            JasperPrint filledReport = JasperFillManager.fillReport(
                    getCompiledReport("dishProductsReportTemplate"),
                    parameters,
                    new DishProductsDataSource(data.dish(), data.servingNumber(), data.constraints())
            );

            return JasperExportManager.exportReportToPdf(filledReport);
        } catch(JRException e) {
            throw new ValidateException("Fail to create dish products report", e).
                    addReason(Rule.of("ReportService.dishReport", failure(Constraint.DOES_NOT_THROW)));
        }
    }

    public byte[] createMenuProductsReport(MenuProductsReportData data) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(JRParameter.REPORT_LOCALE, data.locale());

        try {
            JasperPrint filledReport = JasperFillManager.fillReport(
                    getCompiledReport("menuProductsReportTemplate"),
                    parameters,
                    new MenuProductsDataSource(data.menu(), data.menuNumber(), data.constraints())
            );

            return JasperExportManager.exportReportToPdf(filledReport);
        } catch(JRException e) {
            throw new ValidateException("Fail to create menu products report", e).
                    addReason(Rule.of("ReportService.menuReport", failure(Constraint.DOES_NOT_THROW)));
        }
    }


    private JasperReport loadTemplate(String path) throws IOException, JRException {
        return JasperCompileManager.compileReport(new ClassPathResource(path).getInputStream());
    }

    private JasperReport getCompiledReport(String name) {
        return reportTemplates.stream().
                filter(jr -> jr.getName().equals(name)).
                findFirst().
                orElseThrow();
    }

}
