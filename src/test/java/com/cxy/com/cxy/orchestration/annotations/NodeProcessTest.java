package com.cxy.com.cxy.orchestration.annotations;

import com.cxy.orchestration.annotations.AsReactiveNode;
import com.cxy.orchestration.annotations.From;
import com.cxy.orchestration.annotations.NodeProcessor;
import com.cxy.orchestration.graph.Node;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NodeProcessTest {
    @Test
    public void testSuccess() {
        /**
         * 演示泛型验证的示例工作流
         */
        class GenericWorkflow {
            @AsReactiveNode("a_list_string")
            public List<String> nodeA() {
                return new ArrayList<>(Arrays.asList("hello", "world"));
            }

            @AsReactiveNode("b_list_int")
            public List<Integer> nodeB() {
                return new ArrayList<>(Arrays.asList(1, 2));
            }

            @AsReactiveNode("c_arraylist_string")
            public ArrayList<String> nodeC() {
                return new ArrayList<>(Arrays.asList("sub", "type"));
            }

            // --- 合法的用例 ---

            @AsReactiveNode("legal_exact_match")
            public void legal1(@From("a_list_string") List<String> param) {
                // 合法: List<String> -> List<String>
            }

            @AsReactiveNode("legal_subtype_raw")
            public void legal2(@From("c_arraylist_string") List<String> param) {
                // 合法: ArrayList<String> -> List<String> (子类)
            }

            @AsReactiveNode("legal_wildcard")
            public void legal3(@From("b_list_int") List<? extends Number> param) {
                // 合法: List<Integer> -> List<? extends Number> (通配符上界)
            }

            @AsReactiveNode("legal_raw")
            public void legal4(@From("a_list_string") List param) {
                // 合法: List<String> -> List (非受检赋值)
            }
        }

        // --- 演示 1：成功处理 (包含泛型) ---
        System.out.println("--- 演示 1：成功处理 ---");
        try {
            Map<String, Node> nodeMap = NodeProcessor.process(new GenericWorkflow());
            System.out.println("Successfully processed nodes: " + nodeMap.keySet());
        } catch (Exception e) {
            System.out.println("Processing failed unexpectedly: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testIn() {
        // --- 演示 2：泛型类型不匹配 ---
        System.out.println("\n--- 演示 2：泛型类型不匹配 (List<String> vs List<Integer>) ---");
        class WorkflowIllegalGeneric {
            @AsReactiveNode("a")
            public List<String> nodeA() {
                return null;
            }

            @AsReactiveNode("b_illegal")
            public void nodeB(@From("a") List<Integer> param) { // 错误
            }
        }

        try {
            NodeProcessor.process(new WorkflowIllegalGeneric());
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected validation error:");
            System.out.println("  " + e.getMessage());
        }
    }

    @Test
    public void testInType() {
        // --- 演示 3：原始类型不匹配 (List vs ArrayList) ---
        System.out.println("\n--- 演示 3：原始类型不匹配 (List<String> vs ArrayList<String>) ---");
        class WorkflowIllegalRaw {
            @AsReactiveNode("a")
            public List<String> nodeA() { return null; }

            @AsReactiveNode("b_illegal")
            public void nodeB(@From("a") ArrayList<String> param) { // 错误: List 不能赋值给 ArrayList
            }
        }

        try {
            NodeProcessor.process(new WorkflowIllegalRaw());
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected validation error:");
            System.out.println("  " + e.getMessage());
        }
    }
}
