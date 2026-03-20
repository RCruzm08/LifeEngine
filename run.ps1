$MVN_VERSION = "3.9.6"
$MVN_DIR = "$env:USERPROFILE\.life-engine\maven"
$MVN_BIN = "$MVN_DIR\apache-maven-$MVN_VERSION\bin\mvn.cmd"

function Test-Command($cmd) {
    return $null -ne (Get-Command $cmd -ErrorAction SilentlyContinue)
}

Write-Host ""
Write-Host "  =============================" -ForegroundColor Cyan
Write-Host "     LIFE ENGINE LAUNCHER      " -ForegroundColor Cyan
Write-Host "  =============================" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Command "java")) {
    Write-Host "  [ERRO] Java nao encontrado!" -ForegroundColor Red
    Write-Host "  Instale o JDK 21 em: https://adoptium.net" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "  [OK] Java 21 encontrado." -ForegroundColor Green

$mvnCmd = $null
if (Test-Command "mvn") {
    $mvnCmd = "mvn"
    Write-Host "  [OK] Maven encontrado no PATH." -ForegroundColor Green
} elseif (Test-Path $MVN_BIN) {
    $mvnCmd = $MVN_BIN
    Write-Host "  [OK] Maven local encontrado." -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "  Maven nao encontrado. Instalando automaticamente..." -ForegroundColor Yellow
    $zipUrl = "https://archive.apache.org/dist/maven/maven-3/$MVN_VERSION/binaries/apache-maven-$MVN_VERSION-bin.zip"
    $zipFile = "$env:TEMP\maven.zip"
    New-Item -ItemType Directory -Force -Path $MVN_DIR | Out-Null
    Write-Host "  Baixando Maven $MVN_VERSION..." -ForegroundColor Cyan
    try {
        Invoke-WebRequest -Uri $zipUrl -OutFile $zipFile -UseBasicParsing
        Write-Host "  Extraindo..." -ForegroundColor Cyan
        Expand-Archive -Path $zipFile -DestinationPath $MVN_DIR -Force
        Remove-Item $zipFile
        $mvnCmd = $MVN_BIN
        Write-Host "  [OK] Maven instalado!" -ForegroundColor Green
    } catch {
        Write-Host "  [ERRO] Falha ao baixar Maven." -ForegroundColor Red
        pause
        exit 1
    }
}

Write-Host ""
Write-Host "  Compilando Life Engine..." -ForegroundColor Cyan
Write-Host "  Aguarde o download das dependencias na primeira vez." -ForegroundColor Yellow
Write-Host ""

Set-Location $PSScriptRoot
& $mvnCmd spring-boot:run "-Dspring-boot.run.mainClass=com.lifeengine.LifeEngineApplication"

Write-Host ""
Write-Host "  Servidor encerrado." -ForegroundColor Yellow
