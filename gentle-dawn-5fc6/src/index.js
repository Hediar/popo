/**
 * Welcome to Cloudflare Workers! This is your first worker.
 *
 * - Run `npm run dev` in your terminal to start a development server
 * - Open a browser tab at http://localhost:8787/ to see your worker in action
 * - Run `npm run deploy` to publish your worker
 *
 * Learn more at https://developers.cloudflare.com/workers/
 */

const HEALTH_URL = "https://popo-tumk.onrender.com/health";

export default {
	async scheduled(controller, env, ctx) {
		ctx.waitUntil(pingHealth());
	},

	async fetch(request, env, ctx) {
		return new Response("OK", { status: 200 });
	},
};

async function pingHealth() {
	try {
		const res = await fetch(HEALTH_URL, {
			method: "GET",
			headers: { "user-agent": "popo-cron-worker/1.0" },
		});
		console.log("Health ping status:", res.status);
		if (!res.ok) {
			const body = await res.text().catch(() => "<no body>");
			console.warn("Non-OK response body:", body);
		}
	} catch (err) {
		console.error("Health ping failed:", err);
	}
}
