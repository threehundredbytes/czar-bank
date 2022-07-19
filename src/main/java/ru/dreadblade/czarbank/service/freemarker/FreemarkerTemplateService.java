package ru.dreadblade.czarbank.service.freemarker;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FreemarkerTemplateService {
    private final FreeMarkerConfigurer freeMarkerConfigurer;

    public String getProcessedFreemarkerTemplate(String name, Map<String, Object> model) throws IOException, TemplateException {
        Template freemarkerTemplate = freeMarkerConfigurer.getConfiguration().getTemplate(name);

        return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, model);
    }
}
