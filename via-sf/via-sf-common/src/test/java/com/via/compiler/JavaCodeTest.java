package com.via.compiler;

import java.util.concurrent.atomic.AtomicInteger;

public class JavaCodeTest {

    public final static AtomicInteger SUBFIX = new AtomicInteger(8);

    String getSimpleCode() {
        StringBuilder code = new StringBuilder();
        code.append("package com.via.compiler;");

        code.append("public class HelloServiceImpl" + SUBFIX.getAndIncrement() + " implements HelloService {");
        code.append("   public String sayHello() { ");
        code.append("       return \"Hello world!\"; ");
        code.append("   }");
        code.append("}");
        return code.toString();
    }

    String getSimpleCodeWithoutPackage(){
        StringBuilder code = new StringBuilder();
        code.append("public class HelloServiceImpl" + SUBFIX.getAndIncrement() + "implements org.apache.dubbo.common.compiler.support.HelloService.HelloService {");
        code.append("   public String sayHello() { ");
        code.append("       return \"Hello world!\"; ");
        code.append("   }");
        code.append("}");
        return code.toString();
    }

    String getSimpleCodeWithSyntax(){
        StringBuilder code = new StringBuilder();
        code.append("package com.via.compiler;");

        code.append("public class HelloServiceImpl" + SUBFIX.getAndIncrement() + " implements HelloService {");
        code.append("   public String sayHello() { ");
        code.append("       return \"Hello world!\"; ");
        // code.append("   }");
        // }
        return code.toString();
    }

    // only used for javassist
    String getSimpleCodeWithSyntax0(){
        StringBuilder code = new StringBuilder();
        code.append("package com.via.compiler;");

        code.append("public class HelloServiceImpl_0 implements HelloService {");
        code.append("   public String sayHello() { ");
        code.append("       return \"Hello world!\"; ");
        // code.append("   }");
        // }
        return code.toString();
    }

    String getSimpleCodeWithImports() {
        StringBuilder code = new StringBuilder();
        code.append("package com.via.compiler;");

        code.append("import java.lang.*;\n");
        code.append("import com.via.compiler;\n");

        code.append("public class HelloServiceImpl2" + SUBFIX.getAndIncrement() + " implements HelloService {");
        code.append("   public String sayHello() { ");
        code.append("       return \"Hello world!\"; ");
        code.append("   }");
        code.append("}");
        return code.toString();
    }

    String getSimpleCodeWithWithExtends() {
        StringBuilder code = new StringBuilder();
        code.append("package com.via.compiler;");

        code.append("import java.lang.*;\n");
        code.append("import com.via.compiler;\n");

        code.append("public class HelloServiceImpl" + SUBFIX.getAndIncrement() + " extends org.apache.dubbo.common.compiler.support.HelloServiceImpl0 {\n");
        code.append("   public String sayHello() { ");
        code.append("       return \"Hello world3!\"; ");
        code.append("   }");
        code.append("}");
        return code.toString();
    }
}
