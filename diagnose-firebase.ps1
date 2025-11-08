# Firebase Quick Fix Script

# Run this in PowerShell to diagnose the issue

Write-Host "🔍 Firebase Diagnosis Script" -ForegroundColor Cyan
Write-Host "================================`n" -ForegroundColor Cyan

# 1. Check google-services.json
Write-Host "1️⃣ Checking google-services.json..." -ForegroundColor Yellow
if (Test-Path "app\google-services.json") {
    Write-Host "✅ File exists" -ForegroundColor Green
    $content = Get-Content "app\google-services.json" -Raw | ConvertFrom-Json
    Write-Host "   Project ID: $($content.project_info.project_id)" -ForegroundColor White
    Write-Host "   Firebase URL: $($content.project_info.firebase_url)" -ForegroundColor White
    Write-Host "   Package: $($content.client[0].client_info.android_client_info.package_name)" -ForegroundColor White
    
    if ($content.client[0].client_info.android_client_info.package_name -ne "com.example.respondr") {
        Write-Host "   ⚠️ WARNING: Package name doesn't match!" -ForegroundColor Red
    }
} else {
    Write-Host "❌ google-services.json not found!" -ForegroundColor Red
    Write-Host "   Download it from Firebase Console" -ForegroundColor Yellow
}

Write-Host "`n2️⃣ Checking AndroidManifest permissions..." -ForegroundColor Yellow
$manifest = Get-Content "app\src\main\AndroidManifest.xml" -Raw
if ($manifest -match "android.permission.INTERNET") {
    Write-Host "✅ INTERNET permission found" -ForegroundColor Green
} else {
    Write-Host "❌ INTERNET permission missing!" -ForegroundColor Red
}

Write-Host "`n3️⃣ Checking Firebase dependencies..." -ForegroundColor Yellow
$gradle = Get-Content "app\build.gradle.kts" -Raw
if ($gradle -match "firebase-bom") {
    Write-Host "✅ Firebase BOM found" -ForegroundColor Green
} else {
    Write-Host "❌ Firebase BOM missing!" -ForegroundColor Red
}
if ($gradle -match "firebase-database") {
    Write-Host "✅ Firebase Database found" -ForegroundColor Green
} else {
    Write-Host "❌ Firebase Database missing!" -ForegroundColor Red
}
if ($gradle -match "com.google.gms.google-services") {
    Write-Host "✅ Google Services plugin found" -ForegroundColor Green
} else {
    Write-Host "❌ Google Services plugin missing!" -ForegroundColor Red
}

Write-Host "`n4️⃣ Next steps:" -ForegroundColor Yellow
Write-Host "   1. Go to Firebase Console → Realtime Database" -ForegroundColor White
Write-Host "   2. Check if database exists at the URL shown above" -ForegroundColor White
Write-Host "   3. Go to Rules tab and verify .write is set to true" -ForegroundColor White
Write-Host "   4. Rebuild the app: ./gradlew clean build" -ForegroundColor White
Write-Host "   5. Check Android Studio Logcat for 'FirebaseReportManager' logs" -ForegroundColor White

Write-Host "`n📋 Firebase Database Rules (for testing):" -ForegroundColor Cyan
Write-Host @"
{
  "rules": {
    "emergency_reports": {
      ".read": true,
      ".write": true
    }
  }
}
"@ -ForegroundColor Gray

Write-Host "`n✅ Done! Check output above for issues." -ForegroundColor Green
