import io
import os
import re

directory = 'c:/dev/android_wks/AppCasa'

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt') and 'AppNavigation.kt' not in file and 'Screen.kt' not in file:
            filepath = os.path.join(root, file)
            with io.open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()

            # Reemplazar Screen.XYZ.route -> Screen.XYZ
            new_content = re.sub(r'Screen\.([A-Za-z0-9_]+)\.route', r'Screen.\1', content)
            
            # Reemplazar Screen.XYZ.createRoute(arg) -> Screen.XYZ(arg)
            new_content = re.sub(r'Screen\.([A-Za-z0-9_]+)\.createRoute\((.*?)\)', r'Screen.\1(\2)', new_content)

            # Fix SearchItem in DashboardUseCases
            if 'DashboardUseCases.kt' in filepath:
                new_content = new_content.replace('SearchItem(it.id, it.nombre, SearchType.STOCK, Icons.Default.Inventory, Screen.Inventory.route)', 'SearchItem(it.id, it.nombre, SearchType.STOCK, Icons.Default.Inventory, Screen.Inventory)')

            if new_content != content:
                with io.open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print("Refactored: " + filepath)
