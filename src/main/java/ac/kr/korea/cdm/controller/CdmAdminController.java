package ac.kr.korea.cdm.controller;

import ac.kr.korea.cdm.constants.Constants;
import ac.kr.korea.cdm.dto.CaAppResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@CrossOrigin("*")
@RestController
public class CdmAdminController {
    private static final Logger logger = LoggerFactory.getLogger(CdmAdminController.class);

    @RequestMapping(value = "/admin", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView adminPage(ModelAndView mv) {
        mv.setViewName("admin");
        return mv;
    }

    @RequestMapping(value = "/admin/member-manage", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView memberManage(ModelAndView mv) {
        mv.setViewName("membermanage");
        return mv;
    }

    @RequestMapping(value = "/admin/crl-manage", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView crlManage(ModelAndView mv) {
        mv.setViewName("crlmanage");
        return mv;
    }

    @RequestMapping(value = "/admin/add-crl", method = RequestMethod.POST)
    public ModelAndView addCrl(ModelAndView mv, HttpServletRequest request) throws URISyntaxException, ClientProtocolException, IOException {
        String bs64Cert = request.getParameter("bs64Cert");
        int reason = Integer.parseInt(request.getParameter("reason"));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        URI uri = new URIBuilder(Constants.CA_DOMAIN + "add-crl")
                .setParameter("bs64Cert", bs64Cert)
                .setParameter("reason", reason + "")
                .build();

//		HttpGet httpGet = new HttpGet(uri);
        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

        ResponseHandler<String> handler = new BasicResponseHandler();
        String body = handler.handleResponse(httpResponse);

        ObjectMapper mapper = new ObjectMapper();
        CaAppResponseDto<String> response = mapper.readValue(body, CaAppResponseDto.class);
        logger.info(response.getData());

        mv.setViewName("crlmanage");

        return mv;
    }
}
