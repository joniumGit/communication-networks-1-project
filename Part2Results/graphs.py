import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

plt.switch_backend('tkAgg')

simulation_small: pd.DataFrame = pd.read_csv('40-100-20.csv').rename(columns=lambda s: s.strip())
simulation_medium: pd.DataFrame = pd.read_csv('60-120-20.csv').rename(columns=lambda s: s.strip())
simulation_large: pd.DataFrame = pd.read_csv('100-400-100.csv').rename(columns=lambda s: s.strip())

selection = [
    'Orchestration architecture',
    'Edge devices count',
    'Average execution delay (s)',
    'Tasks successfully executed',
    'Total network traffic (MBytes)',
    'Average VM CPU usage (%)',  # [:6]

    'Average VM CPU usage (Cloud) (%)',
    'Average VM CPU usage (Edge) (%)',
    'Average VM CPU usage (Mist) (%)',

    'Generated tasks'
]

ALL = 'ALL'
CLOUD_ONLY = 'CLOUD_ONLY'
EDGE_AND_CLOUD = 'EDGE_AND_CLOUD'
EDGE_ONLY = 'EDGE_ONLY'
MIST_AND_CLOUD = 'MIST_AND_CLOUD'
MIST_ONLY = 'MIST_ONLY'

ARCH, DEVICE_COUNT, EXEC_DELAY, TASK_SUCCESS_COUNT, TRAFFIC, VM_CPU = selection[:6]
VM_CPU_CLOUD, VM_CPU_EDGE, VM_CPU_MIST, GENERATED_TASKS = selection[6:]

ARCHS = [ALL, CLOUD_ONLY, EDGE_ONLY, MIST_ONLY, MIST_AND_CLOUD, EDGE_AND_CLOUD]
COLOR = ['black', 'blue', 'red', 'green', 'cyan', 'pink']


def plot_part(simulation: pd.DataFrame, ax: plt.Axes, arch: str, color: str, field: str):
    data = simulation[simulation[ARCH] == arch]
    ax.set_title(field)
    if data[field].median() > 0:
        ax.plot(data[DEVICE_COUNT], data[field], label=f"{arch}", color=color)


def make_title(simulation: pd.DataFrame):
    mmin = simulation[DEVICE_COUNT].min()
    mmax = simulation[DEVICE_COUNT].max()
    step = (mmax - mmin) // (simulation.groupby(selection[0])[selection[0]].count().max() - 1)
    return f"Device count: {mmin}-{mmax}-{step}"


def make_legend(ax: plt.Axes, fig: plt.Figure):
    handles, labels = ax.get_legend_handles_labels()
    fig.legend(handles, labels, loc='lower center', ncol=len(labels))


def plot_cpu(simulation: pd.DataFrame, block=False):
    fig: plt.Figure
    axs: np.ndarray[plt.Axes]
    fig, axs = plt.subplots(2, 2)

    fig.suptitle(make_title(simulation))

    for target_arch, c in zip(ARCHS, COLOR):
        for ax, field in zip(axs.flatten(), [VM_CPU, VM_CPU_CLOUD, VM_CPU_EDGE, VM_CPU_MIST]):
            plot_part(simulation, ax, target_arch, c, field)

    make_legend(axs[0, 0], fig)
    plt.show(block=block)


def plot_field(field, block=False):
    fig: plt.Figure
    axs: np.ndarray[plt.Axes]
    fig, axs = plt.subplots(3, 1)

    fig.suptitle(field)
    for ax, simulation in zip(axs.flatten(), [simulation_small, simulation_medium, simulation_large]):

        for target_arch, c in zip(ARCHS, COLOR):
            plot_part(simulation, ax, target_arch, c, field)
            ax.set_title(make_title(simulation))

        make_legend(ax, fig)
        plt.show(block=block)


def plot_success_rate(block=False):
    fig: plt.Figure
    axs: np.ndarray[plt.Axes]
    fig, axs = plt.subplots(3, 1)

    fig.suptitle("Success Rate (Completed / Generated) (%)")
    for ax, simulation in zip(axs.flatten(), [simulation_small, simulation_medium, simulation_large]):

        for target_arch, c in zip(ARCHS, COLOR):
            data = simulation[simulation[ARCH] == target_arch]
            ax.plot(
                data[DEVICE_COUNT],
                data[TASK_SUCCESS_COUNT] / data[GENERATED_TASKS] * 100,
                label=f"{target_arch}",
                color=c
            )
            ax.set_title(make_title(simulation))

        make_legend(ax, fig)
        plt.show(block=block)


if __name__ == '__main__':
    for simu in [simulation_small, simulation_medium, simulation_large]:
        plot_cpu(simu)
    plot_field(TRAFFIC)
    plot_field(EXEC_DELAY)
    plot_success_rate()
    plt.show()
