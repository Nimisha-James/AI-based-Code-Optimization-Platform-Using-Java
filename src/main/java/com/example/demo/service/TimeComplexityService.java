package com.example.demo.service;

import com.example.demo.model.CodeEntry;
import com.example.demo.repository.CodeRepository;
import org.codehaus.janino.SimpleCompiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Service
public class TimeComplexityService {

    @Autowired
    private CodeRepository codeRepository;

    public String analyzeAndStoreComplexity(String code, String userEmail) throws Exception {
        String className = "DynamicCode_" + System.currentTimeMillis();
        String fullCode = generateFullCode(className, code);

        System.out.println("Generated Code: " + fullCode);
        Class<?> compiledClass = compileWithJanino(fullCode);
        System.out.println("Compilation Successful for class: " + compiledClass.getName());

        Map<Integer, Long> executionCounts = measureExecutionCounts(compiledClass);
        System.out.println("Execution Counts: " + executionCounts);
        String complexity = estimateComplexity(executionCounts);
        System.out.println("Detected Complexity: " + complexity);

        CodeEntry entry = new CodeEntry(userEmail, code, complexity,"");
        codeRepository.save(entry);
        return complexity;
    }

    private String generateFullCode(String className, String code) {
        return "public class " + className + " {\n" +
               "    private static long operationCount = 0;\n" +
               "    public static int execute(int n) {\n" +
               "        operationCount = 0;\n" +
               code + "\n" +
               "        return 0;\n" +
               "    }\n" +
               "    public static long getOperationCount() {\n" +
               "        return operationCount;\n" +
               "    }\n" +
               "}";
    }

    private Class<?> compileWithJanino(String code) throws Exception {
        SimpleCompiler compiler = new SimpleCompiler();
        compiler.cook(code);
        return compiler.getClassLoader().loadClass(code.split("class ")[1].split(" ")[0]);
    }

    private Map<Integer, Long> measureExecutionCounts(Class<?> clazz) throws Exception {
        Map<Integer, Long> counts = new HashMap<>();
        // Adjusted input sizes for O(2^n) detection (small values to avoid overflow)
        int[] inputSizes = {5, 10, 15, 20}; // 2^5=32, 2^10=1024, 2^15=32768, 2^20=1048576

        for (int n : inputSizes) {
            Method executeMethod = clazz.getMethod("execute", int.class);
            Method getCountMethod = clazz.getMethod("getOperationCount");

            executeMethod.invoke(null, n);
            long operationCount = (Long) getCountMethod.invoke(null);
            counts.put(n, operationCount);
        }

        return counts;
    }

    private String estimateComplexity(Map<Integer, Long> executionCounts) {
        double[] x = new double[executionCounts.size()];
        double[] y = new double[executionCounts.size()];
        int i = 0;
        for (Map.Entry<Integer, Long> entry : executionCounts.entrySet()) {
            x[i] = entry.getKey(); // Use raw n for exponential check
            y[i] = entry.getValue(); // Raw operation count
            i++;
        }

        // Check for O(2^n) by comparing operations to 2^n
        boolean isExponential = isExponentialGrowth(x, y);
        if (isExponential) return "O(2^n)";

        // Fall back to log-log regression for other cases
        double[] logX = new double[x.length];
        double[] logY = new double[y.length];
        for (i = 0; i < x.length; i++) {
            logX[i] = Math.log(x[i]);
            logY[i] = Math.log(y[i]);
        }

        double a = calculateSlope(logX, logY);
        if (Math.abs(a - 1.0) < 0.2) return "O(n)";
        if (Math.abs(a - 2.0) < 0.2) return "O(n^2)";
        if (a < 0.5) return "O(log n)";
        return "O(n^" + String.format("%.1f", a) + ")";
    }

    private boolean isExponentialGrowth(double[] x, double[] y) {
        if (x.length < 2) return false;
        for (int i = 1; i < x.length; i++) {
            double expectedGrowth = Math.pow(2, x[i]) / Math.pow(2, x[i-1]);
            double actualGrowth = y[i] / (double) y[i-1];
            // Allow some tolerance due to integer approximations
            if (Math.abs(actualGrowth - expectedGrowth) > 0.5) return false;
        }
        return true;
    }

    private double calculateSlope(double[] x, double[] y) {
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = x.length;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }

    public static void main(String[] args) throws Exception {
        TimeComplexityService service = new TimeComplexityService();
        String code = "int count = 0; for (int i = 0; i < (1 << n); i++) { count++; operationCount++; }";
        String complexity = service.analyzeAndStoreComplexity(code, "test@example.com");
        System.out.println("Complexity: " + complexity);
    }
}