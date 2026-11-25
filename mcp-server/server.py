import json
import os
import shutil
import subprocess
from pathlib import Path

from mcp.server.fastmcp import FastMCP

MCP_SERVER_NAME = "java-api-automation"
MCP_CONFIG_PATH = Path(__file__).resolve().parent.parent / ".mcp.json"


def load_mcp_environment() -> dict[str, str]:
    with MCP_CONFIG_PATH.open(encoding="utf-8") as config_file:
        config = json.load(config_file)

    try:
        configured_env = config["mcpServers"][MCP_SERVER_NAME]["env"]
    except (KeyError, TypeError) as error:
        raise ValueError(
            f"Missing env configuration for '{MCP_SERVER_NAME}' "
            f"in {MCP_CONFIG_PATH}"
        ) from error

    if not isinstance(configured_env, dict):
        raise ValueError(
            f"Expected an env object for '{MCP_SERVER_NAME}' in {MCP_CONFIG_PATH}"
        )

    environment = os.environ.copy()
    environment.update(
        {key: str(value) for key, value in configured_env.items()}
    )

    java_home = environment.get("JAVA_HOME")
    if java_home:
        environment["PATH"] = os.pathsep.join(
            [str(Path(java_home) / "bin"), environment.get("PATH", "")]
        )

    return environment


MCP_ENV = load_mcp_environment()
PROJECT_DIR = Path(MCP_ENV["AUTOMATION_PROJECT_DIR"]).expanduser().resolve()
JAVA_HOME = MCP_ENV.get("JAVA_HOME")

mcp = FastMCP("Java API Automation Server")

@mcp.tool()
def check_maven_environment() -> dict:
    """Check Java and Maven environment used by MCP."""

    diagnostics = {}

    diagnostics["java_home"] = JAVA_HOME
    diagnostics["maven_path"] = shutil.which("mvn", path=MCP_ENV.get("PATH"))
    diagnostics["path"] = MCP_ENV.get("PATH")
    diagnostics["project_dir"] = str(PROJECT_DIR)

    for name, command in {
        "java_version": ["java", "-version"],
        "maven_version": ["mvn", "-version"]
    }.items():

        result = subprocess.run(
            command,
            capture_output=True,
            text=True,
            env=MCP_ENV,
            check=False,
        )

        diagnostics[name] = (
            result.stdout + result.stderr
        )

    return diagnostics


@mcp.tool()
def run_api_smoke() -> dict:
    """Execute the existing Maven REST Assured and TestNG smoke suite for qa environment."""

    pom_file = PROJECT_DIR / "pom.xml"

    if not pom_file.exists():
        return {"error": "pom.xml not found"}

    try:
        result = subprocess.run(
            [
                "mvn",
                "clean",
                "test",
                "-Denv=qa",
                "-Psmoke",
                "-DthreadCount=1"
            ],
            cwd=PROJECT_DIR,
            env=MCP_ENV,
            capture_output=True,
            text=True,
            timeout=600,
            shell=False
        )

        return {
            "status": "PASSED" if result.returncode == 0 else "FAILED",
            "exit_code": result.returncode,
            "stdout": result.stdout[-12000:],
            "stderr": result.stderr[-6000:],
            "report_directory": str(
                PROJECT_DIR / "target/surefire-reports"
            )
        }

    except subprocess.TimeoutExpired:
        return {"error": "Test execution timed out"}

    except Exception as e:
        return {"error": str(e)}


if __name__ == "__main__":
    mcp.run(transport="stdio")