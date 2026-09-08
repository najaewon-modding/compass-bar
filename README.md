# Compass Bar

Minecraft의 기본 Player List를 화면 상단의 방향 기반 Compass HUD로 대체하는 NeoForge 모드입니다.

현재 바라보는 방향과 같은 차원에 있는 다른 플레이어의 방향을 한눈에 확인할 수 있습니다.

## 지원 환경

| 항목 | 값 |
| --- | --- |
| Minecraft | `26.1.2` |
| NeoForge | `26.1.2.97` |
| Java | `25` |
| 멀티플레이 | 서버/클라이언트 지원 |

## 주요 기능

- 화면 상단에 Compass Bar 표시
- 카메라 방향에 따라 방위 표시가 실시간으로 이동
- 같은 차원에 있는 다른 플레이어의 방향을 마커로 표시
- 거리에 따라 플레이어 마커 크기 변경
- 플레이어마다 서로 다른 마커 색상 사용
- 같은 월드에서는 재접속 및 서버 재시작 후에도 기존 마커 색상 유지
- 화면 오른쪽 중앙에 현재 플레이어와 자신의 마커 색상 표시
- Player Locator Bar 대신 Experience Bar 표시
- 기본 Player List 키로 Compass HUD 켜기/끄기

### 플레이어 마커

다른 플레이어는 Compass Bar 위에 다이아몬드 형태의 마커로 표시됩니다.

| 거리 | 마커 크기 |
| --- | --- |
| 64블록 이하 | 5 × 5 |
| 64~256블록 | 4 × 4 |
| 256블록 초과 | 3 × 3 |

현재는 **같은 차원에 있는 플레이어만** 표시됩니다.

### 마커 색상

각 플레이어에게는 서로 구분하기 쉬운 색상이 배정됩니다. 같은 월드에서는 재접속하거나 서버를 재시작해도 기존 색상이 유지됩니다.

### Experience Bar 유지

Compass Bar가 플레이어 위치 표시를 대신하므로, 기존 Player Locator Bar 대신 Experience Bar를 항상 표시합니다.

## 조작

기본 Minecraft의 **Player List** 키를 그대로 사용합니다.

기본 키:

```text
Tab
```

- 접속 시 Compass HUD는 기본적으로 켜져 있습니다.
- Player List 키를 한 번 누르면 꺼지고, 다시 누르면 켜집니다.
- Minecraft Controls에서 Player List 키를 변경하면 Compass Bar도 변경된 키를 그대로 사용합니다.

## 설치

### 서버

Compass Bar JAR 파일을 서버의 `mods` 폴더에 넣습니다.

### 클라이언트

Compass HUD를 사용하려는 플레이어는 동일한 JAR 파일을 클라이언트의 `mods` 폴더에 넣습니다.

## 멀티플레이

| 서버 | 클라이언트 | 동작 |
| --- | --- | --- |
| Compass Bar 설치 | Compass Bar 설치 | 모든 기능 사용 |
| Compass Bar 설치 | 모드 없음 | Vanilla UI로 정상 접속 |

서버에 Compass Bar가 설치되어 있어도 모든 플레이어에게 클라이언트 모드 설치가 강제되지는 않습니다. 모드가 없는 클라이언트는 기존 Minecraft UI를 그대로 사용합니다.

## 라이선스

MIT License. 자세한 내용은 [LICENSE](LICENSE)를 확인하세요.
