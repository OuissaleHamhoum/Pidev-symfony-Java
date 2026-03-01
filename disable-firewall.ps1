# Configuration du pare-feu pour Loopi QR Code
Write-Host "🔧 Configuration du pare-feu pour Loopi QR Code" -ForegroundColor Cyan

# Ajouter une règle pour le port 8081
netsh advfirewall firewall add rule name="Loopi QR Code 8081" dir=in action=allow protocol=TCP localport=8081

# Vérifier la règle
Write-Host "`n📋 Règle ajoutée:" -ForegroundColor Yellow
netsh advfirewall firewall show rule name="Loopi QR Code 8081"

# Tester la connexion locale
Write-Host "`n🔍 Test de connexion locale:" -ForegroundColor Cyan
Test-NetConnection -ComputerName localhost -Port 8081

Write-Host "`n✅ Configuration terminée" -ForegroundColor Green
Write-Host "📱 Les utilisateurs peuvent maintenant se connecter via http://10.21.92.26:8081/login" -ForegroundColor Green