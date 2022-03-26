package cure.bo.service.bo;

public class Patient extends User{
    //诊断数据
    protected String listenResult;
    protected String lookResult;
    protected String feelPulseResult;
    protected String questionResult;

    public boolean feel() {
        if("abc123".equals(apiToken)){
            feelPulseResult = username + "切诊结果";
            return true;
        }
        else return false;
    }

    public boolean listen() {
        if("abc123".equals(apiToken)){
            listenResult = username + "闻诊结果";
            return true;
        }
        else return false;
    }

    public boolean look() {
        if("abc123".equals(apiToken)){
            lookResult = username + "望诊结果";
            return true;
        }
        else return false;
    }

    public boolean question() {
        if("abc123".equals(apiToken)){
            questionResult = username + "问诊结果";
            return true;
        }
        else return false;
    }
}
