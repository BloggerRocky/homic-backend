package com.example.homic.aspect;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.annotation.VerifyParam;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.model.UserInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

import static com.example.homic.constants.CodeConstants.*;
import static com.example.homic.constants.NormalConstants.*;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/12.17:51
 * 项目名：homic
 */
//AOP处理
@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {
    private static Logger logger = LoggerFactory.getLogger(GlobalOperationAspect.class);
    //定义切点（切点是一个方法），将需要切入的注解绑定到切点上
    @Pointcut("@annotation(com.example.homic.annotation.GlobalInteceptor)")
    private void requestInterceptor(){
    }
    //为切点绑定事件可以用@Before,@After,@Around,其中Around表示在切点前后均插入事件
    @Around("requestInterceptor()")//在对应拦截器发生前后绑定事件
    public Object interceptorDo(ProceedingJoinPoint point) throws MyException {
        //获取传入该方法的值
        Object[] values = point.getArgs();
        //获取方法签名，其中包含了方法的具体信息
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        //通过方法签名获得方法实例
        Method method = methodSignature.getMethod();
        //获取该方法实例上GlobalInterceptor的注释实例
        GlobalInteceptor annotation = method.getAnnotation(GlobalInteceptor.class);
        try{
            //如果注释中checkParams为true，则进行参数校验
            if(annotation.checkParams())
                validateParams( method,values);
            if(annotation.checkLogin())
                checkLogin(method,values);
            if(annotation.checkAdmin())
                checkAdmin(method,values);
             return point.proceed();
        } catch (MyException e)
        {
            throw  e;
        }
        catch (Throwable e) {
            logger.error("全局拦截器异常");
            throw new RuntimeException(e);
        }

    }

    /**
     * 参数校验
     * @param method
     * @param values
     */
    private void validateParams(Method method,Object[] values) throws MyException {
        Parameter[] parameters = method.getParameters();
        for(int i = 0;i<parameters.length;i++)
        {
            Parameter parameter = parameters[i];
            //如果参数上存在校验注解
            if(parameter.isAnnotationPresent(VerifyParam.class))
            {
                //获取参数上的注解实例
                VerifyParam annotation = parameter.getAnnotation(VerifyParam.class);
                //获取对应传入值
                Object value = values[i];
                String parameterType = parameter.getParameterizedType().getTypeName();
                //将参数类型与预设的参数类型进行比较，查看是否为基本数据类型
                if(ArrayUtils.contains(PARAM_TYPE_ARRAY,parameterType))
                    checkValue(value,annotation);
                else
                    checkObject(value,annotation,parameter);
            }
        }
    }

    /**
     * 对参数中的基本数据进行校验
     * @param value
     * @param annotation
     */
    private void checkValue(Object value,VerifyParam annotation) throws MyException {
        boolean isEmpty = value==null||value.toString().trim().length()==0;
        int length = value == null?0:value.toString().length();
        //为空校验
        if(annotation.required() && isEmpty)
            throw new MyException("空校验参数错误",FAIL_RES_CODE);
        //长度范围校验
        if(annotation.min()>length || annotation.max()<length)
            throw new MyException("长度范围校验参数错误",FAIL_RES_CODE);
        //长度校验
        if(annotation.length() != -1 && annotation.length() != length)
            throw new MyException("长度校验参数错误",FAIL_RES_CODE);
        //正则校验
        if(!isEmpty && annotation.regex()!=null)
        {
            String regex = annotation.regex().getRegex();
            if(!Pattern.matches(regex,value.toString()))
                throw new MyException("正则校验参数错误",FAIL_RES_CODE);
        }

    }
    /**
     * 对参数中的对象数据进行校验
     * @param obj
     * @param annotation
     */
    private void checkObject(Object obj,VerifyParam annotation,Parameter parameter) throws MyException {
        if(annotation.required() && obj == null)
            throw new MyException("空校验参数错误",FAIL_RES_CODE);
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        Class<? extends Type> aClass = parameter.getParameterizedType().getClass();

        for(int i=0;i<declaredFields.length;i++)
        {

        }

    }
   private void checkLogin(Method method,Object[] values) throws MyException {
       Parameter[] parameters = method.getParameters();
       HttpSession session = null;
       for(int i = 0;i<parameters.length;i++)
       {
           Parameter parameter = parameters[i];
           String parameterType = parameter.getParameterizedType().getTypeName();
           //如果找到HttpSession类型参数，将其值赋值给session,跳出循环
            if(parameterType.equals(SESSION_REFERENCE))
           {
                session = (HttpSession) values[i];
                break;
           }
       }
       if(session.getAttribute(SESSION_USER_INFO_KEY) == null)
           throw new MyException("登录超时，请重新登录",OFFLINE_RES_CODE);
   }
    private void checkAdmin(Method method,Object[] values) throws MyException {
        Parameter[] parameters = method.getParameters();
        HttpSession session = null;
        for(int i = 0;i<parameters.length;i++)
        {
            Parameter parameter = parameters[i];
            String parameterType = parameter.getParameterizedType().getTypeName();
            //如果找到HttpSession类型参数，将其值赋值给session,跳出循环
            if(parameterType.equals(SESSION_REFERENCE))
            {
                session = (HttpSession) values[i];
                break;
            }
        }
        if(session.getAttribute(SESSION_USER_INFO_KEY) == null || !((SessionWebUserDTO)session.getAttribute(SESSION_USER_INFO_KEY)).getAdmin() )
            throw new MyException("你没有圈权限执行此操作",FAIL_RES_CODE);
    }
}
