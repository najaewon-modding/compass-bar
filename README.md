# Player Compass Bar

**Player Compass Bar**는 Minecraft의 기존 Player List 대신, 화면 상단에 **Compass Bar**를 표시하는 NeoForge 모드입니다.

`Tab` 키로 Compass Bar를 on/off 할 수 있으며, 현재 바라보는 방향과 같은 dimension에 있는 다른 플레이어의 방향을 표시합니다.

## 주요 기능

* 화면 상단에 Compass Bar 표시
* 현재 camera 방향 표시
* 접속 중인 다른 플레이어의 방향 표시
* `Tab` 키를 이용한 on/off toggle
* 같은 dimension의 플레이어만 표시
* Server에서 Client로 player position 동기화
* Client 모드 설치는 optional

## Client / Server 동작

| Server | Client | 동작                              |
| ------ | ------ | ------------------------------- |
| 설치     | 설치     | Compass Bar와 player marker 사용   |
| 설치     | 미설치    | 기존 vanilla Player List 사용       |
| 미설치    | 설치     | Server 기반 player tracking 사용 불가 |

Server는 모드를 지원하는 Client에게만 player position 정보를 전송합니다.

따라서 Server에 모드가 설치되어 있어도 Client 설치를 강제하지 않으며, 모드가 없는 Client에서는 기존 `Tab` Player List가 그대로 동작합니다.

## 조작

`Tab` 키를 한 번 누르면 Compass Bar가 켜지고, 다시 누르면 꺼집니다.

```
Tab -> ON
Tab -> OFF
```

모드가 설치된 Client에서는 Compass Bar를 사용하는 동안 기존 Player List를 표시하지 않습니다.

## 구현 방향

가능한 한 **Mixin 없이 NeoForge API와 event를 사용**하여 구현합니다.

구현은 크게 다음 세 부분으로 구성됩니다.

* Client: Compass Bar rendering, `Tab` input, player direction 계산
* Server: 접속 중인 player position 수집
* Network: Server-to-Client player position 동기화

## 개발 환경

* Minecraft: `26.1.2`
* Mod Loader: `NeoForge`
* Side: `Client + Server`
* Client Installation: `Optional`

> 현재 개발 중인 프로젝트입니다.
