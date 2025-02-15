#!/usr/bin/env python
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

import os
import re
from pathlib import Path
from typing import List, Set

class chinese_character_check_test:
    CHINESE_CHAR_PATTERN = re.compile(r'[\u4e00-\u9fa5]')
    # Exclude directories or files. If it is a file, just write the file name. The same is true for directories, just write the directory name.
    EXCLUDED_DIRS_AND_FILES = {
        "target",
        "node_modules",
        "dist",
    }
    # Supported file extensions
    SUPPORTED_EXTENSIONS = {
        ".java",
        ".kt",
        ".scala",
        ".js",
        ".jsx",
        ".ts",
        ".tsx",
        ".vue"
    }

    def should_not_contain_chinese_in_comments(self):
        violations = self.scan_for_chinese_characters(scan_target.COMMENTS)
        self.assert_no_chinese_characters(violations)

    def scan_for_chinese_characters(self, target: 'scan_target') -> List[str]:
        violations = []
        for ext in self.SUPPORTED_EXTENSIONS:
            for path in Path("..").rglob(f"*{ext}"):
                if self.is_valid_file(path) and not self.is_excluded(path):
                    self.process_file(path, target, violations)
        return violations

    def is_excluded(self, path: Path) -> bool:
        path_str = str(path)
        return any(excluded in path_str for excluded in self.EXCLUDED_DIRS_AND_FILES)

    def is_valid_file(self, path: Path) -> bool:
        path_str = str(path)
        return any(path_str.endswith(ext) for ext in self.SUPPORTED_EXTENSIONS)

    def process_file(self, path: Path, target: 'scan_target', violations: List[str]):
        try:
            with open(path, 'r', encoding='utf-8') as file:
                content = file.read()
                if target.include_comments():
                    self.check_comments(content, path, violations)
                if target.include_code():
                    self.check_code(content, path, violations)
        except Exception as e:
            print(f"Error processing file: {path}")
            print(e)

    def check_comments(self, content: str, path: Path, violations: List[str]):
        # Matching multiple types of comments
        comment_patterns = [
            r'//.*?$',  # Single line comments
            r'/\*.*?\*/',  # Multi line comments
            r'<!--.*?-->'  # Vue/HTML,/javascript/typescript comments
        ]
        for pattern in comment_patterns:
            for comment in re.findall(pattern, content, re.DOTALL | re.MULTILINE):
                if self.CHINESE_CHAR_PATTERN.search(comment):
                    violations.append(self.format_violation(path, "comment", comment.strip()))

    def check_code(self, content: str, path: Path, violations: List[str]):
        # Matching string literals in multiple languages
        string_patterns = [
            r'"[^"]*"',  # Double quoted strings
            r"'[^']*'"   # Single quoted strings
        ]
        for pattern in string_patterns:
            for string_literal in re.findall(pattern, content):
                if self.CHINESE_CHAR_PATTERN.search(string_literal):
                    violations.append(self.format_violation(path, "code", string_literal))

    def format_violation(self, path: Path, location: str, content: str) -> str:
        return f"Chinese characters found in {location} at {path.absolute()}: {content}"

    def assert_no_chinese_characters(self, violations: List[str]):
        assert len(violations) == 0, f"Found Chinese characters in files:\n{os.linesep.join(violations)}"

class scan_target:
    def __init__(self, check_comments: bool, check_code: bool):
        self.check_comments = check_comments
        self.check_code = check_code

    def include_comments(self) -> bool:
        return self.check_comments

    def include_code(self) -> bool:
        return self.check_code

scan_target.COMMENTS = scan_target(True, False)
scan_target.CODE = scan_target(False, True)
scan_target.ALL = scan_target(True, True)

if __name__ == "__main__":
    test = chinese_character_check_test()
    test.should_not_contain_chinese_in_comments()
