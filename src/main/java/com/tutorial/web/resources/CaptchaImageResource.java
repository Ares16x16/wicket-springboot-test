package com.tutorial.web.resources;

import com.google.code.kaptcha.impl.DefaultKaptcha;

import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.DynamicImageResource;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class CaptchaImageResource extends DynamicImageResource {
    private final DefaultKaptcha captchaProducer;
    private String generatedText;
    private final UUID uuid = UUID.randomUUID();

    public CaptchaImageResource(DefaultKaptcha captchaProducer) {
        this.captchaProducer = captchaProducer;
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            generatedText = captchaProducer.createText();
            ImageIO.write(captchaProducer.createImage(generatedText), "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public String getGeneratedText() {
        return generatedText;
    }

    @Override
    protected void setResponseHeaders(ResourceResponse response, Attributes attributes) {
        super.setResponseHeaders(response, attributes);
        response.disableCaching();
        WebResponse webResponse = (WebResponse) attributes.getResponse();
        webResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        webResponse.setHeader("Pragma", "no-cache");
        webResponse.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
        webResponse.setHeader("ETag", uuid.toString());
    }
}
