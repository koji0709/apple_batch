package com.sgswit.fx.utils.web;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoWSolver {
    private static List<Integer> findNumbers(int low, int high, int parts, BigInteger target, long startTime, long timeoutMs) {
        return dfs(new ArrayList<>(), BigInteger.ONE, low, low, high, parts, target, startTime, timeoutMs);
    }

    private static List<Integer> dfs(List<Integer> current, BigInteger product, int last, int low, int high, int parts,
                                     BigInteger target, long startTime, long timeoutMs) {
        if (System.currentTimeMillis() - startTime > timeoutMs) return null;

        if (current.size() == parts) {
            if (product.equals(target)) return new ArrayList<>(current);
            return null;
        }
        for (int i = last; i <= high; i++) {
            if (i < low) continue;
            BigInteger newProduct = product.multiply(BigInteger.valueOf(i));
            if (newProduct.compareTo(target) > 0) continue;
            if (!target.mod(newProduct).equals(BigInteger.ZERO) && current.size() + 1 < parts) continue;

            current.add(i);
            List<Integer> found = dfs(current, newProduct, i, low, high, parts, target, startTime, timeoutMs);
            if (found != null) return found;
            current.remove(current.size() - 1);
        }
        return null;
    }
    public static Map<String,Object> solvePoW(int low, int high, int parts, BigInteger result, long timeoutMs) {
        Map<String,Object> res = new HashMap<>();
        long startTime = System.currentTimeMillis();
        List<Integer> numbers = findNumbers(low, high, parts, result, startTime, timeoutMs);

        res.put("numbers",numbers);
        res.put("took",System.currentTimeMillis() - startTime);

        return res;
    }
}
