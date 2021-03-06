package eu.geoknow.batch;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class ServiceTasklet implements Tasklet, InitializingBean {

    private static Logger log = Logger.getLogger(ServiceTasklet.class);

    private String service;
    private String body;
    private String contenttype;
    private String method;

    public ServiceTasklet() {
    }

    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub

    }

    public RepeatStatus execute(StepContribution stepcontribution, ChunkContext chunkcontext)
            throws Exception {

        log.info(service);

        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", contenttype);
        headers.add("Accept", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);

        log.debug(requestEntity.toString());

        try {
            ResponseEntity<String> responseEntity = rest.exchange(service, HttpMethod
                    .valueOf(method.toUpperCase()), requestEntity, String.class);
            log.debug(responseEntity.getBody());
            //set step status based on HTTP status
            HttpStatus code = responseEntity.getStatusCode();
            log.debug(code);
            //if ok, return 
            if(code == HttpStatus.OK) return RepeatStatus.FINISHED;
            if(code == HttpStatus.CREATED) return RepeatStatus.FINISHED;
            //else set error code and exit with failure
        	stepcontribution.setExitStatus(ExitStatus.FAILED);
        	throw new UnexpectedJobExecutionException("Service responded with an error code: "+code);
        } catch (HttpClientErrorException e) {
            stepcontribution.setExitStatus(ExitStatus.FAILED);
            e.printStackTrace();
            throw new UnexpectedJobExecutionException(e.getMessage());
        }
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getContenttype() {
        return contenttype;
    }

    public void setContenttype(String contenttype) {
        this.contenttype = contenttype;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

}
