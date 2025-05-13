#include <iostream>
#include <cstring>
#include "inf_int.h"
#include <vector>

inf_int::inf_int() : digits(nullptr), length(1), thesign(true) {
    digits = new char[2];
    digits[0] = '0';
    digits[1] = '\0';
}

inf_int::inf_int(int n) {
    thesign = (n >= 0);
    n = std::abs(n);

    char buffer[100];
    int i = 0;

    while (n > 0) {
        buffer[i] = (n % 10) + '0';
        n /= 10;
        i++;
    }

    if (i == 0) {
        new(this) inf_int();
    }
    else {
        digits = new char[i];
        for (int j = 0; j < i; j++) {
            digits[j] = buffer[j];
        }
        length = i;
    }
}

inf_int::inf_int(const char* str) {
    if (str[0] == '-') {
        thesign = false;
        str++; 
    }
    else {
        thesign = true;
    }

    length = strlen(str);
    if (!thesign) length--;
    digits = new char[length];

    if (str[0] == '-') {
        for (int i = 1; i <= length; i++) {
            if (str[i] == '\0') break;
            else digits[i - 1] = str[length - i + 1];
        }
    }
    else {
        for (int i = 0; i < length; i++) {
            if (str[i] == '\0') break;
            else digits[i] = str[length - i - 1];
        }
    }
}

inf_int::inf_int(const inf_int& other) : length(other.length), thesign(other.thesign) {
    digits = new char[length];
    for (int i = 0; i < length; i++) {
        digits[i] = other.digits[i];
    }
}

inf_int::~inf_int() {
    delete[] digits;
}

inf_int& inf_int::operator=(const inf_int& other) {
    if (this != &other) {
        delete[] digits;
        length = other.length;
        thesign = other.thesign;
        digits = new char[length];
        for (int i = 0; i < length; i++) {
            digits[i] = other.digits[i];
        }
    }
    return *this;
}

bool operator==(const inf_int& a, const inf_int& b) {
    if (a.length != b.length || a.thesign != b.thesign) {
        return false;
    }

    for (int i = 0; i < a.length; i++) {
        if (a.digits[i] != b.digits[i]) {
            return false;
        }
    }

    return true;
}

bool operator!=(const inf_int& a, const inf_int& b) {
    return !(a == b);
}

bool operator>(const inf_int& a, const inf_int& b) {
    if (a.thesign && !b.thesign) return true;
    if (!a.thesign && b.thesign) return false;

    if (a.thesign) {
        if (a.length > b.length) return true;
        if (a.length < b.length) return false;
        for (int i = a.length - 1; i >= 0; i--) {
            if (a.digits[i] > b.digits[i]) return true;
            if (a.digits[i] < b.digits[i]) return false;
        }
        return false;
    }
    else {
        if (a.length > b.length) return false;
        if (a.length < b.length) return true;
        for (int i = a.length - 1; i >= 0; i--) {
            if (a.digits[i] > b.digits[i]) return false;
            if (a.digits[i] < b.digits[i]) return true;
        }
        return false;
    }
}

bool operator<(const inf_int& a, const inf_int& b) {
    return !(a > b) && !(a == b);
}

inf_int operator+(const inf_int& a, const inf_int& b) {
    inf_int result;
    std::string tmp;
    int carry = 0;

    if (a.thesign != b.thesign) {
        if (a.thesign) {
            result = b;
            result.thesign = true;
            return a - result;
        }
        else {
            result = a;
            result.thesign = true;
            return b - result;
        }
    }

    int max_length = std::max(a.length, b.length);

    for (int i = 0; i < max_length || carry; i++) {
        int sum = carry;

        if (i < a.length)
            sum += a.digits[i] - '0';
        if (i < b.length)
            sum += b.digits[i] - '0';

        carry = sum / 10;
        tmp += (sum % 10) + '0';
    }

    int numOfZero = 0;
    for (int i = tmp.length() - 1; i >= 0; i--) {
        if (tmp[i] == '0') numOfZero++;
        else break;
    }
    tmp = tmp.substr(0, tmp.length() - numOfZero);

    result.digits = new char[tmp.length()];
    result.length = tmp.length();
    for (int i = 0; i < tmp.length(); i++) {
        result.digits[i] = tmp[i];
    }
    result.thesign = a.thesign;
    return result;
}

inf_int operator-(const inf_int& a, const inf_int& b) {
    inf_int result;
    std::string tmp;
    std::string tmp2;
    int carry = 0;

    if (a == b) {
        result = inf_int();
        return result;
    }

    if (a.thesign != b.thesign) {
        if (a.thesign) {
            result = b;
            result.thesign = true;
            return a + result;
        }
        else {
            result = b;
            result.thesign = false;
            return a + result;
        }
    }

    if (a > b) {
        for (int i = 0; i < b.length; i++) {
            int subtraction = (a.digits[i] - '0') - (b.digits[i] - '0') - carry;
            if (subtraction < 0) {
                subtraction = subtraction + 10;
                carry = 1;
            }
            else {
                carry = 0;
            }
            tmp += subtraction + '0';
        }

        for (int i = b.length; i < a.length; i++) {
            int subtraction = (a.digits[i] - '0') - carry;
            if (subtraction < 0) {
                subtraction = subtraction + 10;
                carry = 1;
            }
            else {
                carry = 0;
            }
            tmp += subtraction + '0';
        }
        int numOfZero = 0;
        for (int i = tmp.length() - 1; i >= 0; i--) {
            if (tmp[i] == '0') numOfZero++;
            else break;
        }
        tmp = tmp.substr(0, tmp.length() - numOfZero);

        result.digits = new char[tmp.length()];
        result.length = tmp.length();
        for (int i = 0; i < tmp.length(); i++) {
            result.digits[i] = tmp[i];
        }
        if (a.thesign) result.thesign = true;
        else result.thesign = false;
        return result;
    }
    else {
        for (int i = 0; i < a.length; i++) {
            int subtraction = (b.digits[i] - '0') - (a.digits[i] - '0') - carry;
            if (subtraction < 0) {
                subtraction = subtraction + 10;
                carry = 1;
            }
            else {
                carry = 0;
            }
            tmp += subtraction + '0';
        }

        for (int i = a.length; i < b.length; i++) {
            int subtraction = (b.digits[i] - '0') - carry;
            if (subtraction < 0) {
                subtraction = subtraction + 10;
                carry = 1;
            }
            else {
                carry = 0;
            }
            tmp += subtraction + '0';
        }
        int numOfZero = 0;
        for (int i = tmp.length() - 1; i >= 0; i--) {
            if (tmp[i] == '0') numOfZero++;
            else break;
        }
        tmp = tmp.substr(0, tmp.length() - numOfZero);

        result.digits = new char[tmp.length()];
        result.length = tmp.length();
        for (int i = 0; i < tmp.length(); i++) {
            result.digits[i] = tmp[i];
        }
        if (a.thesign) result.thesign = false;
        else result.thesign = true;
        return result;
    }
}

inf_int operator*(const inf_int& a, const inf_int& b) {
    int len_a = a.length;
    int len_b = b.length;
    int len_result = len_a + len_b;
    inf_int result;
    result.length = len_result;
    result.thesign = (a.thesign == b.thesign);

   
    result.digits = new char[len_result + 1];
    for (int i = 0; i < len_result; i++) {
        result.digits[i] = '0';
    }
    result.digits[len_result] = '\0';

    for (int i = 0; i < len_a; i++) {
        int carry = 0;
        for (int j = 0; j < len_b; j++) {
            int product = (a.digits[i] - '0') * (b.digits[j] - '0') + (result.digits[i + j] - '0') + carry;
            carry = product / 10;
            result.digits[i + j] = (product % 10) + '0';
        }
        int k = i + len_b;
        while (carry > 0) {
            int new_digit = (result.digits[k] - '0') + carry;
            carry = new_digit / 10;
            result.digits[k] = (new_digit % 10) + '0';
            k++;
        }
    }

    int non_zero_index = len_result - 1;
    while (non_zero_index >= 0 && result.digits[non_zero_index] == '0') {
        non_zero_index--;
    }

    if (non_zero_index < 0) {
        result.length = 1;
        result.thesign = true;
        delete[] result.digits;
        result.digits = new char[2];
        result.digits[0] = '0';
        result.digits[1] = '\0';
    }
    else {
        result.length = non_zero_index + 1;
        char* new_digits = new char[result.length + 1];
        for (int i = 0; i < result.length; i++) {
            new_digits[i] = result.digits[i];
        }
        new_digits[result.length] = '\0';
        delete[] result.digits;
        result.digits = new_digits;
    }

    return result;
}


std::ostream& operator<<(std::ostream& os, const inf_int& a) {
    if (!a.thesign) {
        os << '-';
    }
    for (int i = a.length - 1; i >= 0; i--) {
        os << a.digits[i];
    }
    return os;
}
