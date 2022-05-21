package Parser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LLParser {
    private static String nextSymbol;
    private static int index = 0;
    private static ArrayList<Pair<String, String>> symbolList;
    private static HashMap<String, Integer> symbolMap;
    private static String dotFormat = "";
    private static ArrayList<String> valueList;

    public static void createDotGraph(String fileName)
    {
        for (String v : valueList) {
            if (v.charAt(0) > 'a' && v.charAt(0) < 'z') {
                if (v.length() > 2 && v.startsWith("eps"))
                    dotFormat += v + "[color=red];";
                else
                    dotFormat += v + "[color=green];";
            }
        }

        GraphViz gv=new GraphViz();
        gv.addln(gv.start_graph());
        gv.add(dotFormat);
        gv.addln(gv.end_graph());
        String type = "png";
        gv.decreaseDpi();
        gv.decreaseDpi();

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString() + "/src/Parser/" + fileName + "."+ type;
        File out = new File(s);
        gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
    }

    /**
     * 부모 노드의 번호, 부모노드 이름, 자식노드 이름을 입력해 그래프에 간선을 하나 추가하는 메소드
     * @param parentNum 부모노드의 번호
     * @param parentNode 부모노드의 이름
     * @param childNode 자식노드의 이름
     * @param childValue 자식노드가 id, Constant, Type인 경우 Value 입력, 그렇지 않을 땐 빈 문자열 "" 입력
     */
    static void addConnection(int parentNum, String parentNode, String childNode, String childValue) {
        symbolMap.put(childNode, symbolMap.get(childNode) + 1);
        String valueNode = "";

        if (childValue.equals(""))
            valueNode = childNode + symbolMap.get(childNode);
        else
            valueNode = childNode + symbolMap.get(childNode) + "_" + childValue;

        dotFormat += parentNode + parentNum + "->" + valueNode + ";";
        valueList.add(valueNode);
    }

    static void parse(ArrayList<Pair<String, String>> tokenList) {
        symbolList = tokenList;
        symbolList.add(new Pair<>("$", ""));
        nextSymbol = symbolList.get(index).getKey();
        valueList = new ArrayList<>();

        symbolMap = new HashMap<>();

        /**
         * GraphViz로 제작된 그래프에는 노드의 이름에 특수문자가 포함될 수 없습니다.
         * 따라서 각 터미널 기호들은 GraphViz로 출력시 다음과 같은 이름을 사용하도록 코드를 작성해주세요.
         *
         * || or / && and / == eq / != neq / < ll / <= le / > gg / >= ge /
         * + sum / - sub / * mul / '/' div / % mod / ! not / ( lP / ) rP /
         * Constant con / this ths / ReadInteger rdI / ReadLine rdL / new new /
         * id id / newArray nwA / , com / Type typ / epsilon eps
         *
         * 논터미널 기호의 이름은 첫 글자가 알파벳 대문자인 영문을 사용하도록 코드를 작성해주세요.
         */
        String[] symbolArray = new String[]{"E", "Ep", "F", "Fp", "G", "Gp", "H", "Hp", "I", "Ip", "J", "Jp", "K",
        "or", "and", "eq", "neq", "ll", "le", "gg", "ge", "sum", "sub", "mul", "div", "mod", "not", "lP", "rP",
        "con", "ths", "rdI", "rdL", "new", "id", "nwA", "typ", "com", "eps"};

        /**
         * 같은 이름의 노드가 새로 생성될 시 번호를 붙여 구분하기 위한 symbolMap 생성.
         * Key : 노드 이름 (터미널, 논터미널 기호)
         * Value : 0부터 시작, 같은 기호의 노드가 하나씩 생성될 때마다 +1
         */
        for (String symbol : symbolArray) {
            symbolMap.put(symbol, 0);
        }

        symbolMap.put("E", 1);
        PE(symbolMap.get("E"));

        if(nextSymbol.equals("$")) {
            System.out.println("Accept");
            createDotGraph("parsetree");
        } else {
            error("$");
        }
    }

    /**
     * 논터미널 기호 E
     */
    static void PE(int currentNodeNum) {
        if (nextSymbol.equals("-") || nextSymbol.equals("!") || nextSymbol.equals("(") || nextSymbol.equals("Constant") ||
                nextSymbol.equals("this") || nextSymbol.equals("ReadInteger") || nextSymbol.equals("ReadLine") ||
                nextSymbol.equals("new") || nextSymbol.equals("id") || nextSymbol.equals("NewArray")) {
            addConnection(currentNodeNum,"E", "F", "");
            PF(symbolMap.get("F"));
            addConnection(currentNodeNum,"E", "Ep", "");
            PE_prime(symbolMap.get("Ep"));
        }
        else {
            error("FIRST set of Non-terminal \'E\'");
        }
    }

    /**
     * 논터미널 기호 E'
     */
    static void PE_prime(int currentNodeNum) {
        if (nextSymbol.equals("||")) {
            addConnection(currentNodeNum, "Ep", "or", "");
            or();
            addConnection(currentNodeNum, "Ep", "F", "");
            PF(symbolMap.get("F"));
            addConnection(currentNodeNum, "Ep", "Ep", "");
            PE_prime(symbolMap.get("Ep"));
        } else if (nextSymbol.equals(")") || nextSymbol.equals(",") || nextSymbol.equals("$")) {
            addConnection(currentNodeNum, "Ep", "eps", "");
            epsilon();
        } else {
            error("|| or FOLLOW set of Non-terminal \'E'\'");
        }
    }

    /**
     * 논터미널 기호 F
     * 예시 코드에서만 정상 작동. 정답 코드에선 수정 필요.
     */
    static void PF(int currentNodeNum) {
        if (nextSymbol.equals("-") || nextSymbol.equals("!") || nextSymbol.equals("(") || nextSymbol.equals("Constant") ||
                nextSymbol.equals("this") || nextSymbol.equals("ReadInteger") || nextSymbol.equals("ReadLine") ||
                nextSymbol.equals("new") || nextSymbol.equals("id") || nextSymbol.equals("NewArray")) {
            addConnection(currentNodeNum,"F", "G", "");
            PG(symbolMap.get("G"));
            addConnection(currentNodeNum, "F", "Fp", "");
            PF_prime(symbolMap.get("Fp"));
        }
        else {
            error("FIRST set of Non-terminal \'F\'");
        }
    }
    
    /**
     * Non-terminal F`
     */
    static void PF_prime(int currentNodeNum) {
    	if(nextSymbol.equals("&&")) {
    		addConnection(currentNodeNum, "Fp", "and", "");
    		and();
    		addConnection(currentNodeNum, "Fp", "G", "");
    		PG(symbolMap.get("G"));
    		addConnection(currentNodeNum, "Fp", "Fp", "");
    		PF_prime(symbolMap.get("Fp")); 
        } else if (nextSymbol.equals("||") || nextSymbol.equals(")") || nextSymbol.equals(",") || nextSymbol.equals("$")) {
            addConnection(currentNodeNum, "Fp", "eps", "");
            epsilon();
        } else {
            error("&& or FOLLOW set of Non-terminal \'F'\'");
    	}
    }
    /**
     * Non-terminal G
     */
    static void PG(int currentNodeNum) {
        if (nextSymbol.equals("-") || nextSymbol.equals("!") || nextSymbol.equals("(") || nextSymbol.equals("Constant") ||
                nextSymbol.equals("this") || nextSymbol.equals("ReadInteger") || nextSymbol.equals("ReadLine") ||
                nextSymbol.equals("new") || nextSymbol.equals("id") || nextSymbol.equals("NewArray")) {
            addConnection(currentNodeNum,"G", "H", "");
            PH(symbolMap.get("H"));
            addConnection(currentNodeNum, "G", "Gp", "");
            PG_prime(symbolMap.get("Gp"));
        }
        else {
            error("FIRST set of Non-terminal \'G\'");
        }
    }
    /**
     * Non-terminal G`
     */
    static void PG_prime(int currentNodeNum) {
    	if(nextSymbol.equals("==")) {
    		addConnection(currentNodeNum, "Gp", "eq", "");
    		eq();
    		addConnection(currentNodeNum, "Gp", "H", "");
    		PH(symbolMap.get("G"));
    		addConnection(currentNodeNum, "Gp", "Gp", "");
    		PG_prime(symbolMap.get("Fp")); 
    	} else if(nextSymbol.equals("!=")) {
    		addConnection(currentNodeNum, "Gp", "neq", "");
    		eq();
    		addConnection(currentNodeNum, "Gp", "H", "");
    		PH(symbolMap.get("G"));
    		addConnection(currentNodeNum, "Gp", "Gp", "");
    		PG_prime(symbolMap.get("Fp")); 
        } else if (nextSymbol.equals("||") || nextSymbol.equals("&&") || nextSymbol.equals(")") || nextSymbol.equals(",") || nextSymbol.equals("$")) {
            addConnection(currentNodeNum, "Gp", "eps", "");
            epsilon();
        } else {
            error("==, != or FOLLOW set of Non-terminal \'G'\'");
    	}
    }
    /**
     * Non-terminal H
     */
    static void PH(int currentNodeNum) {
    	if (nextSymbol.equals("-") || nextSymbol.equals("!") || nextSymbol.equals("(") || nextSymbol.equals("Constant") ||
                nextSymbol.equals("this") || nextSymbol.equals("ReadInteger") || nextSymbol.equals("ReadLine") ||
                nextSymbol.equals("new") || nextSymbol.equals("id") || nextSymbol.equals("NewArray")) {
            addConnection(currentNodeNum,"H", "I", "");
            PI(symbolMap.get("I"));
            addConnection(currentNodeNum, "H", "Hp", "");
            PH_prime(symbolMap.get("Hp"));
        }
        else {
            error("FIRST set of Non-terminal \'H\'");
        }
    }
    /**
     * Non-terminal H`
     */
    static void PH_prime(int currentNodeNum) {
    	if(nextSymbol.equals("<")) {
    		addConnection(currentNodeNum, "Hp", "ll", "");
    		ll();
    		addConnection(currentNodeNum, "Hp", "I", "");
    		PI(symbolMap.get("I"));
    		addConnection(currentNodeNum, "Hp", "Hp", "");
    		PH_prime(symbolMap.get("Hp")); 
    	} else if(nextSymbol.equals("<=")) {
    		addConnection(currentNodeNum, "Hp", "le", "");
    		le();
    		addConnection(currentNodeNum, "Hp", "I", "");
    		PI(symbolMap.get("I"));
    		addConnection(currentNodeNum, "Hp", "Hp", "");
    		PH_prime(symbolMap.get("Hp")); 
    	} else if(nextSymbol.equals(">")) {
    		addConnection(currentNodeNum, "Hp", "gg", "");
    		gg();
    		addConnection(currentNodeNum, "Hp", "I", "");
    		PI(symbolMap.get("I"));
    		addConnection(currentNodeNum, "Hp", "Hp", "");
    		PH_prime(symbolMap.get("Hp")); 
    	} else if(nextSymbol.equals(">=")) {
    		addConnection(currentNodeNum, "Hp", "ge", "");
    		ge();
    		addConnection(currentNodeNum, "Hp", "I", "");
    		PI(symbolMap.get("I"));
    		addConnection(currentNodeNum, "Hp", "Hp", "");
    		PH_prime(symbolMap.get("Hp")); 
        } else if (nextSymbol.equals("||") || nextSymbol.equals("&&") || nextSymbol.equals("==") || nextSymbol.equals("!=") || 
        		nextSymbol.equals(")") || nextSymbol.equals(",") || nextSymbol.equals("$")) {
            addConnection(currentNodeNum, "Hp", "eps", "");
            epsilon();
        } else {
            error("<, <=, >, >= or FOLLOW set of Non-terminal \'H'\'");
    	}
    }
    /**
     * Non-terminal I
     */
    static void PI(int currentNodeNum) {
    	if (nextSymbol.equals("-") || nextSymbol.equals("!") || nextSymbol.equals("(") || nextSymbol.equals("Constant") ||
                nextSymbol.equals("this") || nextSymbol.equals("ReadInteger") || nextSymbol.equals("ReadLine") ||
                nextSymbol.equals("new") || nextSymbol.equals("id") || nextSymbol.equals("NewArray")) {
            addConnection(currentNodeNum,"I", "J", "");
            PJ(symbolMap.get("J"));
            addConnection(currentNodeNum, "I", "Ip", "");
            PI_prime(symbolMap.get("Ip"));
        }
        else {
            error("FIRST set of Non-terminal \'I\'");
        }
    }
    /**
     * Non-terminal I`
     */
    static void PI_prime(int currentNodeNum) {
    	if(nextSymbol.equals("+")) {
    		addConnection(currentNodeNum, "Ip", "sum", "");
    		sum();
    		addConnection(currentNodeNum, "Ip", "J", "");
    		PJ(symbolMap.get("J"));
    		addConnection(currentNodeNum, "Ip", "Ip", "");
    		PI_prime(symbolMap.get("Ip")); 
    	} else if(nextSymbol.equals("-")) {
    		addConnection(currentNodeNum, "Ip", "sub", "");
    		sub();
    		addConnection(currentNodeNum, "Ip", "J", "");
    		PJ(symbolMap.get("J"));
    		addConnection(currentNodeNum, "Ip", "Ip", "");
    		PI_prime(symbolMap.get("Ip")); 
    	} else if (nextSymbol.equals("||") || nextSymbol.equals("&&") || nextSymbol.equals("==") || nextSymbol.equals("!=") ||
    			nextSymbol.equals("<") || nextSymbol.equals("<=") || nextSymbol.equals(">") || nextSymbol.equals(">=") ||
    			nextSymbol.equals(")") || nextSymbol.equals(",") || nextSymbol.equals("$")) {
            addConnection(currentNodeNum, "Ip", "eps", "");
            epsilon();
        } else {
            error("+, - or FOLLOW set of Non-terminal \'I'\'");
    	}
    }
    /**
     * Non-terminal J
     */
    static void PJ(int currentNodeNum) {
    	if (nextSymbol.equals("-") || nextSymbol.equals("!") || nextSymbol.equals("(") || nextSymbol.equals("Constant") ||
                nextSymbol.equals("this") || nextSymbol.equals("ReadInteger") || nextSymbol.equals("ReadLine") ||
                nextSymbol.equals("new") || nextSymbol.equals("id") || nextSymbol.equals("NewArray")) {
            addConnection(currentNodeNum,"J", "K", "");
            PK(symbolMap.get("K"));
            addConnection(currentNodeNum, "J", "Jp", "");
            PJ_prime(symbolMap.get("Jp"));
        }
        else {
            error("FIRST set of Non-terminal \'J\'");
        }
    }
    /**
     * Non-terminal J`
     */
    static void PJ_prime(int currentNodeNum) {
    	if(nextSymbol.equals("*")) {
    		addConnection(currentNodeNum, "Jp", "mul", "");
    		mul();
    		addConnection(currentNodeNum, "Jp", "K", "");
    		PK(symbolMap.get("K"));
    		addConnection(currentNodeNum, "Jp", "Jp", "");
    		PJ_prime(symbolMap.get("Jp")); 
    	} else if(nextSymbol.equals("/")) {
    		addConnection(currentNodeNum, "Jp", "div", "");
    		div();
    		addConnection(currentNodeNum, "Jp", "K", "");
    		PK(symbolMap.get("K"));
    		addConnection(currentNodeNum, "Jp", "Jp", "");
    		PJ_prime(symbolMap.get("Jp")); 
    	} else if(nextSymbol.equals("%")) {
    		addConnection(currentNodeNum, "Jp", "mod", "");
    		mod();
    		addConnection(currentNodeNum, "Jp", "K", "");
    		PK(symbolMap.get("K"));
    		addConnection(currentNodeNum, "Jp", "Jp", "");
    		PJ_prime(symbolMap.get("Jp")); 
    	} else if (nextSymbol.equals("||") || nextSymbol.equals("&&") || nextSymbol.equals("==") || nextSymbol.equals("!=") ||
    			nextSymbol.equals("<") || nextSymbol.equals("<=") || nextSymbol.equals(">") || nextSymbol.equals(">=") ||
    			nextSymbol.equals("+") || nextSymbol.equals("-") ||
    			nextSymbol.equals(")") || nextSymbol.equals(",") || nextSymbol.equals("$")) {
            addConnection(currentNodeNum, "Jp", "eps", "");
            epsilon();
        } else {
            error("*, /, % or FOLLOW set of Non-terminal \'J'\'");
    	}
    }
    /**
     * Non-terminal K
     */
    static void PK(int currentNodeNum) {
    	if(nextSymbol.equals("-")) {
    		addConnection(currentNodeNum, "K", "sub", "");
    		sub();
    		addConnection(currentNodeNum, "K", "K", "");
    		PK(symbolMap.get("K"));
    	} else if(nextSymbol.equals("!")) {
    		addConnection(currentNodeNum, "K", "not", "");
    		not();
    		addConnection(currentNodeNum, "K", "K", "");
    		PK(symbolMap.get("K"));
    	} else if(nextSymbol.equals("(")) {
    		addConnection(currentNodeNum, "K", "lP", "");
    		lP();
    		addConnection(currentNodeNum, "K", "K", "");
    		PK(symbolMap.get("K"));
    		addConnection(currentNodeNum, "K", "rP", "");
    		rP();
    	} else if(nextSymbol.equals("Constant")) {
    		addConnection(currentNodeNum, "K", "con", symbolList.get(index).getValue());
    		con();
    	} else if(nextSymbol.equals("id")) {
    		addConnection(currentNodeNum, "K", "id", symbolList.get(index).getValue());
    		id();
    	} else if(nextSymbol.equals("this")) {
    		addConnection(currentNodeNum, "K", "ths", "");
    		ths();
    	} else if(nextSymbol.equals("ReadInteger")) {
    		addConnection(currentNodeNum, "K", "rdI", "");
    		rdI();
    		addConnection(currentNodeNum, "K", "lP", "");
    		lP();
    		addConnection(currentNodeNum, "K", "rP", "");
    		rP();
    	} else if(nextSymbol.equals("ReadLine")) {
    		addConnection(currentNodeNum, "K", "rdL", "");
    		rdL();
    		addConnection(currentNodeNum, "K", "lP", "");
    		lP();
    		addConnection(currentNodeNum, "K", "rP", "");
    		rP();
    	} else if(nextSymbol.equals("new")) {
    		addConnection(currentNodeNum, "K", "nw", "");
    		nw();
    		addConnection(currentNodeNum, "K", "nw", "");
    		ths();
    	} else if(nextSymbol.equals("NewArray")) {
    		addConnection(currentNodeNum, "K", "nwA", "");
    		nwA();
    		addConnection(currentNodeNum, "K", "lP", "");
    		lP();
    		addConnection(currentNodeNum, "K", "E", "");
    		PE(symbolMap.get("E"));
    		addConnection(currentNodeNum, "K", "com", "");
    		com();
    		addConnection(currentNodeNum, "K", "typ", symbolList.get(index).getValue());
    		typ();
    		addConnection(currentNodeNum, "K", "rP", "");
    		rP();
    	} else {
            error("-, !, (, Constant, id, this, ReadInteger, ReadLine, new, NewArray or FOLLOW set of Non-terminal \'K\'");
    	}
    }

    /**
     * 터미널 기호 or
     */
    static void or() {
        if (nextSymbol.equals("||")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("||");
    }
    
    
    /**
     * terminal and
     */
    static void and() {
        if (nextSymbol.equals("&&")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("&&");
    }
    
    /**
     * terminal eq 
     */
    static void eq() {
        if (nextSymbol.equals("==")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("==");
    }
    
    /**
     * terminal neq 
     */
    static void neq() {
        if (nextSymbol.equals("!=")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("!=");
    }
    
    /**
     * terminal ll
     */
    static void ll() {
        if (nextSymbol.equals("<")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("<");
    }
    
    /**
     * terminal le
     */
    static void le() {
        if (nextSymbol.equals("<=")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("<=");
    }
    
    /**
     * terminal gg
     */
    static void gg() {
        if (nextSymbol.equals(">")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error(">");
    }
    
    /**
     * terminal ge
     */
    static void ge() {
        if (nextSymbol.equals(">=")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error(">=");
    }
    
    /**
     * terminal sum
     */
    static void sum() {
        if (nextSymbol.equals("+")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("+");
    }
    
    /**
     * terminal sub
     */
    static void sub() {
        if (nextSymbol.equals("-")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("-");
    }
    
    /**
     * terminal mul
     */
    static void mul() {
        if (nextSymbol.equals("*")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("*");
    }
    
    /**
     * terminal div
     */
    static void div() {
        if (nextSymbol.equals("/")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("/");
    }
    
    /**
     * terminal mod
     */
    static void mod() {
        if (nextSymbol.equals("%")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("%");
    }
    
    /**
     * terminal not
     */
    static void not() {
        if (nextSymbol.equals("!")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("!");
    }
    
    /**
     * terminal lP
     */
    static void lP() {
        if (nextSymbol.equals("(")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("(");
    }
    
    /**
     * terminal rP
     */
    static void rP() {
        if (nextSymbol.equals(")")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error(")");
    }
    
    /**
     * terminal con
     */
    static void con() {
        if (nextSymbol.equals("Constant")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("Constant");
    }
    
    /**
     * terminal gg
     */
    static void ths() {
        if (nextSymbol.equals("this")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("this");
    }
    
    /**
     * terminal rdI
     */
    static void rdI() {
        if (nextSymbol.equals("ReadInteger")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("ReadInteger");
    }
    
    /**
     * terminal rdL
     */
    static void rdL() {
        if (nextSymbol.equals("ReadLine")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("ReadLine");
    }
    
    /**
     * terminal new
     */
    static void nw() {
        if (nextSymbol.equals("new")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("new");
    }

    /**
     * 터미널 기호 id
     */
    static void id() {
        if (nextSymbol.equals("id")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("id");
    }
    
    /**
     * terminal nwA
     */
    static void nwA() {
        if (nextSymbol.equals("newArray")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("newArray");
    }

    /**
     * terminal com
     */
    static void com() {
        if (nextSymbol.equals(",")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error(",");
    }
    
    /**
     * terminal typ
     */
    static void typ() {
        if (nextSymbol.equals("Type")) {
            if (index < symbolList.size() - 1)
                nextSymbol = symbolList.get(++index).getKey();
        }
        else
            error("Type");
    }
    
    /**
     * 터미널 기호 epsilon
     */
    static void epsilon() {}

    static void error(String expected) {
        System.out.println("Invalid input. Expected " + expected + ", but received " + nextSymbol + " at index " + index + ".");
        System.exit(-1);
    }
}
