# Compass Bar

Minecraft의 기본 Player List를 방향 기반 Compass HUD로 대체하는 NeoForge 모드입니다.

화면 상단의 Compass Bar를 통해 현재 바라보는 방향과 접속 중인 다른 플레이어의 방향을 확인할 수 있습니다.

## 주요 기능

- 화면 상단에 Compass Bar 표시
- 카메라 방향에 따라 Compass가 실시간으로 이동
- Player List 키를 눌러 Compass HUD ON/OFF
- 접속 중인 다른 플레이어의 방향을 Compass 위에 표시
- 플레이어와의 거리에 따라 marker 크기 변경
- 플레이어마다 서로 다른 marker 색상 사용
- 같은 게임 세션에서는 동일한 플레이어의 색상 유지
- 화면 오른쪽 중앙에 현재 플레이어와 marker 색상 표시
- Player Locator Bar 대신 Experience Bar를 항상 표시
- Vanilla Player List 대신 Compass HUD 사용

## Player Marker

다른 플레이어는 Compass Bar 위에 diamond marker로 표시됩니다.

Marker 크기는 플레이어와의 거리에 따라 달라집니다.

| 거리 | Marker 크기 |
| --- | --- |
| 64 blocks 이하 | 5 × 5 |
| 64 ~ 256 blocks | 4 × 4 |
| 256 blocks 초과 | 3 × 3 |

각 플레이어에게는 고유한 색상이 임시로 배정됩니다.

색상은 게임을 종료할 때까지 UUID를 기준으로 유지되며, 게임을 다시 실행하면 새롭게 배정됩니다.

현재는 같은 dimension에 있는 플레이어만 Compass Bar에 표시됩니다.

## 조작

기본 Minecraft의 **Player List** 키를 사용합니다.

기본 키:

`Tab`

한 번 누르면 Compass HUD가 켜지고, 다시 누르면 꺼집니다.

Minecraft Controls에서 Player List 키를 변경하면 Compass Bar도 변경된 키를 그대로 사용합니다.

## Client / Server 동작

Compass Bar는 Server와 Client 양쪽에서 사용할 수 있도록 만들어져 있습니다.

Server는 플레이어 위치 정보를 지원되는 Client에 전달하고, Client는 해당 정보를 이용해 Compass HUD를 렌더링합니다.

| Server | Client | 동작 |
| --- | --- | --- |
| Compass Bar 설치 | Compass Bar 설치 | 모든 기능 사용 |
| Compass Bar 설치 | 모드 없음 | Vanilla 상태로 정상 접속 |

Client에 모드가 설치되어 있지 않은 경우 Server는 Compass Bar 전용 player position packet을 보내지 않습니다.

따라서 Server에 Compass Bar가 설치되어 있어도 Vanilla Client는 별도의 Client 모드 설치 없이 접속할 수 있으며, 기존 Minecraft UI가 그대로 유지됩니다.

## 설치

Minecraft **26.1.2**와 해당 버전의 **NeoForge**가 필요합니다.

### Server

Compass Bar `.jar` 파일을 Server의 `mods` 폴더에 넣습니다.

### Client

Compass HUD를 사용하려는 플레이어는 동일한 `.jar` 파일을 Client의 `mods` 폴더에 넣습니다.

Client에 모드를 설치하지 않아도 Compass Bar가 설치된 Server에 접속할 수 있습니다.

## 개발 환경

- Minecraft 26.1.2
- NeoForge
- Java 21
- Gradle