package com.juggle.im.android.server.beans;

/**
 * 文件凭证响应Bean
 * 对应/jim/file_cred接口返回的data结构
 * 
 * 支持的存储类型：
 * 1: QiNiu（七牛）
 * 2: S3
 * 3: Minio
 * 4: Oss（阿里云）
 */
public class FileCredResp {
    private int oss_type;
    private QiNiuCredResp qiniu_resp;
    private PreSignResp pre_sign_resp;

    public int getOss_type() {
        return oss_type;
    }

    public void setOss_type(int oss_type) {
        this.oss_type = oss_type;
    }

    public QiNiuCredResp getQiniu_resp() {
        return qiniu_resp;
    }

    public void setQiniu_resp(QiNiuCredResp qiniu_resp) {
        this.qiniu_resp = qiniu_resp;
    }

    public PreSignResp getPre_sign_resp() {
        return pre_sign_resp;
    }

    public void setPre_sign_resp(PreSignResp pre_sign_resp) {
        this.pre_sign_resp = pre_sign_resp;
    }

    /**
     * 七牛凭证响应
     */
    public static class QiNiuCredResp {
        private String domain;
        private String token;

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    /**
     * 预签名响应
     */
    public static class PreSignResp {
        private String url;
        private String obj_key;
        private String policy;
        private String sign_version;
        private String credential;
        private String date;
        private String signature;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getObj_key() {
            return obj_key;
        }

        public void setObj_key(String obj_key) {
            this.obj_key = obj_key;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public String getSign_version() {
            return sign_version;
        }

        public void setSign_version(String sign_version) {
            this.sign_version = sign_version;
        }

        public String getCredential() {
            return credential;
        }

        public void setCredential(String credential) {
            this.credential = credential;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }
    }
}
