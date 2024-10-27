package io.innospots.app.visitor.operator;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.innospots.app.core.converter.AppDefinitionConverter;
import io.innospots.app.core.entity.AppDefinitionEntity;
import io.innospots.app.core.model.AppDefinition;
import io.innospots.app.core.operator.AppDefinitionOperator;
import io.innospots.app.visitor.model.AppToken;
import io.innospots.app.visitor.model.RequestAccess;
import io.innospots.base.crypto.IEncryptor;
import io.innospots.base.model.response.R;
import io.innospots.base.model.response.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/28
 */
@Component
public class AppShowVisitor {

    private static final Logger log = LoggerFactory.getLogger(AppShowVisitor.class);
    private final AppDefinitionOperator appDefinitionOperator;
    private final IEncryptor encryptor;

    public AppShowVisitor(AppDefinitionOperator appDefinitionOperator, IEncryptor encryptor) {
        this.appDefinitionOperator = appDefinitionOperator;
        this.encryptor = encryptor;
    }

    public AppDefinition getAppDefinitionByAppPath(String appPath) {
        QueryWrapper<AppDefinitionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(AppDefinitionEntity::getAppPath, appPath);
        AppDefinitionEntity appDefinitionEntity = appDefinitionOperator.getOne(qw);
        return AppDefinitionConverter.INSTANCE.entityToModel(appDefinitionEntity);
    }

    public R<AppToken> checkAccess(String appPath, RequestAccess requestAccess) {
        AppToken appToken = new AppToken();
        appToken.setTs(System.currentTimeMillis());
        R<AppToken> response = new R<>();
        response.setBody(appToken);
        QueryWrapper<AppDefinitionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(AppDefinitionEntity::getAppPath, appPath);
        AppDefinitionEntity appDefinitionEntity = appDefinitionOperator.getOne(qw);
        if (appDefinitionEntity == null) {
            response.fillResponse(ResponseCode.RESOURCE_NOT_EXIST);
            return response;
        }
        AppDefinition appDefinition = AppDefinitionConverter.INSTANCE.entityToModel(appDefinitionEntity);
        appToken.setAppKey(appDefinition.getAppKey());

        Boolean publicVisit = appDefinition.getSettings().getPublicVisit();
        if (publicVisit == null || !publicVisit) {
            response.fillResponse(ResponseCode.AUTH_ACCESS_FORBIDDEN);
            return response;
        }
        if (appDefinition.getAccessKey() != null) {
            if (requestAccess.getAccessKey() == null) {
                response.fillResponse(ResponseCode.AUTH_TOKEN_INVALID);
                return response;
            }
            String decodeAccessKey = encryptor.decode(requestAccess.getAccessKey());
            String compositeKey = appDefinition.getAccessKey() + "-" + requestAccess.getSign();
            if (!Objects.equals(decodeAccessKey, compositeKey)) {
                response.fillResponse(ResponseCode.AUTH_FAILED);
                return response;
            }
        }//end if
        fillToken(appToken,appDefinition);
        if(appToken.getToken()!=null){
            response.fillResponse(ResponseCode.SUCCESS);
        }else {
            response.fillResponse(ResponseCode.AUTH_TOKEN_INVALID);
        }
        return response;
    }

    private void fillToken(AppToken authToken,AppDefinition appDefinition){

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT)
                .build();
        Date expirationDate = new Date(authToken.getTs()+1000*60*60);


        // create JWT Claims
        JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder()
                .subject(appDefinition.getCreatedBy())
                .issuer(appDefinition.getVendor())
                .issueTime(new Date(authToken.getTs()))
                .audience("Innospots")
                .jwtID(HexUtil.encodeHexStr(RandomUtil.randomString(16)))
                .claim("app_key", appDefinition.getAppKey())
                .claim("app_path", appDefinition.getAppPath())
                .expirationTime(expirationDate);

        JWTClaimsSet claimsSet = claimsSetBuilder.build();
        try{
            // create singer
            JWSSigner signer = new MACSigner("ad069f0180fee10b0a5172ba1fa7cda2c8e81b09cda96fafe3cbb134048a8e65");

            // create JWT
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);

            // build jwt
            String jwtString = signedJWT.serialize();
            authToken.setToken(jwtString);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

}
