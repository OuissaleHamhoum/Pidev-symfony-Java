# Désactiver le pare-feu pour le profil privé (WiFi)
Write-Host "🔧 Configuration du pare-feu..." -ForegroundColor Cyan

# Ajouter une règle pour le port 8080
netsh advfirewall firewall add rule name="Loopi QR Code" dir=in action=allow protocol=TCP localport=8080

# Vérifier la règle
Write-Host "`n📋 Règles ajoutées:" -ForegroundColor Yellow
netsh advfirewall firewall show rule name="Loopi QR Code"

# Tester la connexion
Write-Host "`n🔍 Test de connexion:" -ForegroundColor Cyan
Test-NetConnection -ComputerName 10.21.92.26 -Port 8080

Write-Host "`n✅ Configuration terminée" -ForegroundColor Green