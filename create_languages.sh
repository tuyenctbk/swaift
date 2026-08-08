#!/bin/bash
set -e

echo "Generating complete localization translations for all 71 languages..."
python3 generate_translations.py
echo "Translation generation complete!"
