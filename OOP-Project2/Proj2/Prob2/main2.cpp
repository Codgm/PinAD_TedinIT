#include "inf_int.h"
#include <iostream>
#include <sstream>
#include <vector>

int main() {
    while (true) {
        std::string input;
        std::cout << "Input: ";
        std::getline(std::cin, input);

        if (input == "0") {
            break;
        }

        std::istringstream iss(input);
        std::vector<std::string> tokens;
        std::string token;

        // Tokenize the input
        while (iss >> token) {
            tokens.push_back(token);
        }

        if (tokens.size() != 3) {
            std::cout << "Error" << std::endl;
            continue;
        }

        // Remove unnecessary parentheses
        if (tokens[0][0] == '(' && tokens[0][1] == '-' && tokens[0].back() == ')') {
            tokens[0] = tokens[0].substr(2, tokens[0].size() - 3);
        }

        if (tokens[2][0] == '(' && tokens[2][1] == '-' && tokens[2].back() == ')') {
            tokens[2] = tokens[2].substr(2, tokens[2].size() - 3);
        }

        // Check the validity of the numbers
        for (const std::string& num : { tokens[0], tokens[2] }) {
            bool isNegative = (num[0] == '-');
            for (size_t i = isNegative ? 1 : 0; i < num.length(); i++) {
                if (num[i] < '0' || num[i] > '9') {
                    std::cout << "Error" << std::endl;
                    continue;
                }
            }
        }

        inf_int num1(tokens[0].c_str());
        inf_int num2(tokens[2].c_str());

        if (tokens[1] == "+") {
            std::cout << "Output: " << num1 + num2 << std::endl;
        }
        else if (tokens[1] == "-") {
            std::cout << "Output: " << num1 - num2 << std::endl;
        }
        else if (tokens[1] == "*") {
            std::cout << "Output: " << num1 * num2 << std::endl;
        }
        else {
            std::cout << "Error" << std::endl;
        }
    }

    return 0;
}
