#!/bin/bash
# Сборка и запуск Library Management System
# Запускать из папки с файлами проекта

JAR="sqlite-jdbc-3.53.1.0.jar"

echo "==> Компиляция..."
javac -cp ".:lib/sqlite-jdbc-3.53.1.0.jar" src/*.java

java -cp ".:src:lib/sqlite-jdbc-3.53.1.0.jar" Main

if [ $? -ne 0 ]; then
  echo "Ошибка компиляции."
  exit 1
fi

echo "==> Запуск..."
java -cp .:$JAR LibraryApp
