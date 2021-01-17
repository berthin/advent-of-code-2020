data = new File("day19.in") as String[]

MAX_N_RULES = 150
MAX_LEN_MESSAGE = 100
MAX_GROUPS_PER_RULE = 4
MAX_RULES_PER_GROUP = 3

isLeaf = { it.contains('"') }
isLeafCleaned = { it.getClass() == "".getClass() }

// Build graph: rules are composed of groups of rules
buildNeighbors = { content ->
    groups = content.findAll(~/(\d+[ ]?)+/)
    groups.collect { group ->
        group.split().collect { it as int }
    }
}

rulesRaw = data.takeWhile { it !=  "" }.sort(){ it.split(":")[0] as int }

rules = [null] * MAX_N_RULES

rulesRaw.each { rule ->
    (id, content) = rule.split(":").collect { it.trim() }
    rules[id as int] = isLeaf(content) ? content.replace('"', '') : buildNeighbors(content)
}

// Comment the following two lines for PartI
rules[8] = [[42], [42, 8]]
rules[11] = [[42, 31], [42, 11, 31]]

// Determines if a rule is a leaf node (points to a character only, e.g. 14: "b")
isRuleLeaf = (0..rules.size()-1).collect { isLeafCleaned(rules[it]) }

TRUE = 1  ///< any random value
FALSE = 2 ///< any random value different than TRUE
dp_f = new int[MAX_N_RULES][MAX_GROUPS_PER_RULE][MAX_RULES_PER_GROUP][MAX_LEN_MESSAGE][MAX_LEN_MESSAGE]
dp_g = new int[MAX_N_RULES][MAX_LEN_MESSAGE][MAX_LEN_MESSAGE]

// Determines if a part of a message m[i..j] is valid if we start evaluating with the rule at position iGR from the
// iG-th group of the iR-th rule
// 20: 4 5 7 | 3 4 6 <- 20th rule, has two groups
//               ^
// E.g. f(m, 20, 1, 1, i, j) starts evaluating from the second group of rule 20
//
// m : message
// iR: rule Index
// iG: group Index
// iRG: index of a rule within a group
// i, j: limit the bounds of the message m
int f(m, iR, iG, iRG, i, j) {
    if (i == j) return rules[iR][iG].size() == iRG ? TRUE : FALSE

    if (dp_f[iR][iG][iRG][i][j] != 0) return dp_f[iR][iG][iRG][i][j]

    if (rules[iR][iG].size() == iRG + 1) return g(m, rules[iR][iG][iRG], i, j)

    dp_f[iR][iG][iRG][i][j] = (i+1..j).any { k ->
        g(m, rules[iR][iG][iRG], i, k) == TRUE && f(m, iR, iG, iRG + 1, k, j) == TRUE
    } ? TRUE : FALSE

    return dp_f[iR][iG][iRG][i][j]
}

// Determines is a part of a message m[i..j] if valid if we start from the r-th rule
// m: message
// r: rule index
// i, j: limit the bounds of the message m
int g(m, r, i, j) {
    if (isRuleLeaf[r]) return (i + 1 == j) && m[i] == rules[r] ? TRUE : FALSE

    if (dp_g[r][i][j] != 0) return dp_g[r][i][j]

    dp_g[r][i][j] = (0..rules[r].size() - 1).any { iR -> f(m, r, iR, 0, i, j) == TRUE } ? TRUE : FALSE
    return dp_g[r][i][j]
}

int solve(data) {
    messages = data[(rulesRaw.size() + 1)..-1]
    messages.sum { message ->
        // @TODO: One optimization is to use lazy initalization to avoid resetting the buffers
        dp_f = new int[MAX_N_RULES][MAX_GROUPS_PER_RULE][MAX_RULES_PER_GROUP][MAX_LEN_MESSAGE][MAX_LEN_MESSAGE]
        dp_g = new int[MAX_N_RULES][MAX_LEN_MESSAGE][MAX_LEN_MESSAGE]
        r = g(message, 0, 0, message.size()) == TRUE ? 1 : 0
        println "$message, $r"
        r
    }
}

println "Answer ${solve(data)}"
