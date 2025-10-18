package com.cxy.orchestration.annotations;

// --- 所有类的定义 (Node, AsNode, From, NodeProcessor) 放在这里 ---
// ... (请复制上面的 Node.java, AsNode.java, From.java, 和 *新版* NodeProcessor.java 的内容)

import reactor.core.publisher.Mono;
import java.util.function.Function;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List; // 导入 List
import java.util.ArrayList; // 导入 ArrayList

/**
 * 演示泛型验证的示例工作流
 */
class GenericWorkflow {

    @AsNode("a_list_string")
    public List<String> nodeA() {
        return new ArrayList<>(Arrays.asList("hello", "world"));
    }

    @AsNode("b_list_int")
    public List<Integer> nodeB() {
        return new ArrayList<>(Arrays.asList(1, 2));
    }

    @AsNode("c_arraylist_string")
    public ArrayList<String> nodeC() {
        return new ArrayList<>(Arrays.asList("sub", "type"));
    }

    // --- 合法的用例 ---

    @AsNode("legal_exact_match")
    public void legal1(@From("a_list_string") List<String> param) {
        // 合法: List<String> -> List<String>
    }

    @AsNode("legal_subtype_raw")
    public void legal2(@From("c_arraylist_string") List<String> param) {
        // 合法: ArrayList<String> -> List<String> (子类)
    }

    @AsNode("legal_wildcard")
    public void legal3(@From("b_list_int") List<? extends Number> param) {
        // 合法: List<Integer> -> List<? extends Number> (通配符上界)
    }

    @AsNode("legal_raw")
    public void legal4(@From("a_list_string") List param) {
        // 合法: List<String> -> List (非受检赋值)
    }
}

/**
 * 主程序，用于演示泛型验证
 */
public class Main {
    public static void main(String[] args) {

        // --- 演示 1：成功处理 (包含泛型) ---
        System.out.println("--- 演示 1：成功处理 ---");
        NodeProcessor processor = new NodeProcessor();
        try {
            Map<String, Node> nodeMap = processor.process(new GenericWorkflow());
            System.out.println("Successfully processed nodes: " + nodeMap.keySet());
        } catch (Exception e) {
            System.out.println("Processing failed unexpectedly: " + e.getMessage());
            e.printStackTrace();
        }

        // --- 演示 2：泛型类型不匹配 ---
        System.out.println("\n--- 演示 2：泛型类型不匹配 (List<String> vs List<Integer>) ---");
        class WorkflowIllegalGeneric {
            @AsNode("a")
            public List<String> nodeA() {
                return null;
            }

            @AsNode("b_illegal")
            public void nodeB(@From("a") List<Integer> param) { // 错误
            }
        }

        try {
            processor.process(new WorkflowIllegalGeneric());
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected validation error:");
            System.out.println("  " + e.getMessage());
        }

        // --- 演示 3：原始类型不匹配 (List vs ArrayList) ---
        System.out.println("\n--- 演示 3：原始类型不匹配 (List<String> vs ArrayList<String>) ---");
        class WorkflowIllegalRaw {
            @AsNode("a")
            public List<String> nodeA() { return null; }

            @AsNode("b_illegal")
            public void nodeB(@From("a") ArrayList<String> param) { // 错误: List 不能赋值给 ArrayList
            }
        }

        try {
            processor.process(new WorkflowIllegalRaw());
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected validation error:");
            System.out.println("  " + e.getMessage());
        }
    }
}